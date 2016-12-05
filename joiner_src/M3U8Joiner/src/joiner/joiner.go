package joiner

import (
	"fmt"
	"io/ioutil"
	"loader"
	"m3u8"
	"os"
)

func NewOptions() Options {
	opt := Options{}
	return opt
}

type Options struct {
	*loader.HttpOpts
	FileName string //end file url, without ext
	Threads  int    //threads on download
	TempDir  string //temp directory for save all parts
}

type Joiner struct {
	opt     *Options
	list    *m3u8.List
	finish  bool
	loading bool

	loaderFiles *loaderList
}

func NewJoiner(opt Options) *Joiner {
	if opt.Threads == 0 {
		opt.Threads = 1
	}
	if opt.FileName == "" {
		panic("Error file not defined")
	}
	if opt.TempDir == "" {
		panic("Error temp dir not defined")
	}
	j := new(Joiner)
	j.opt = &opt
	return j
}

func (j *Joiner) Stop() {
	j.finish = true
	if j.loaderFiles != nil {
		j.loaderFiles.stop()
	}
}

func (j *Joiner) IsFinish() bool {
	return j.finish
}

func (j *Joiner) LoadUrlList() error {
	http := loader.NewHttp(j.opt.HttpOpts)
	err := http.Connect()
	if err != nil {
		return err
	}
	j.list, err = m3u8.ParseHttp(http)
	if err != nil {
		return err
	}
	j.writeLocalList()
	return nil
}

func (j *Joiner) LoadLocalList() error {
	return j.readLocalList()
}

func (j *Joiner) GetList() *m3u8.List {
	return j.list
}

func (j *Joiner) GetLoaded() (int, int) {
	if j.loaderFiles != nil {
		return j.loaderFiles.loaded(), j.loaderFiles.size()
	}
	return -1, -1
}

func (j *Joiner) DownloadFiles(cfg map[int]bool) error {
	j.finish = false
	j.loaderFiles = newLoader(j.list, cfg, j.opt)
	return j.loaderFiles.download(nil)
}

func (j *Joiner) JoinFiles(cfg map[int]bool) error {
	j.finish = false
	defer func() { j.finish = true }()
	if len(j.list.Content) > 0 {
		err := j.joinList(j.list)
		if err != nil {
			return err
		}
	}
	for i := 0; i < len(j.list.Lists); i++ {
		if cfg == nil {
			err := j.joinList(j.list.Lists[i])
			if err != nil || j.finish {
				return err
			}
		} else if ok, b := cfg[i]; ok && b {
			err := j.joinList(j.list.Lists[i])
			if err != nil || j.finish {
				return err
			}
		}
	}
	return nil
}

func (j *Joiner) RemoveTemp() error {
	return os.RemoveAll(j.opt.TempDir)
}

func (j *Joiner) joinList(l *m3u8.List) error {
	filename := j.opt.FileName + ".mp4"
	if l.Bandwidth != 0 {
		filename = fmt.Sprintf("%s.%d.mp4", j.opt.FileName, l.Bandwidth)
	}
	file, err := os.Create(filename)
	if err != nil {
		return err
	}
	defer file.Close()
	for _, s := range l.Content {
		tmpSubDir := ""
		if l.Bandwidth != 0 {
			tmpSubDir = fmt.Sprint(l.Bandwidth)
		}
		tmpFilename := getTempFilename(j.opt.TempDir, s, tmpSubDir)
		buf, err := ioutil.ReadFile(tmpFilename)
		if err != nil || j.finish {
			return err
		}
		_, err = file.Write(buf)
		if err != nil || j.finish {
			return err
		}
		file.Sync()
	}
	return nil
}
