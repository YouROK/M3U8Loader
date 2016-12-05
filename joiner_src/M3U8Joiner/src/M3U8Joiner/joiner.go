package M3U8Joiner

import (
	"joiner"
	"loader"
	"os"
)

type Options struct {
	FileName string //end file, mp4
	Threads  int    //threads on download
	TempDir  string //temp directory for save all parts
	Url      string
	Timeout  int
	headers  map[string]string
}

type Joiner struct {
	jj      *joiner.Joiner
	local   bool
	cfg     map[int]bool
	err     error
	stage   int
	stoped  bool
	loading bool
}

func NewOptions() *Options {
	opt := new(Options)
	return opt
}

func (c *Options) SetHeader(key, val string) {
	if c.headers == nil {
		c.headers = make(map[string]string)
	}
	c.headers[key] = val
}

func NewJoiner(opt *Options) *Joiner {
	o := joiner.NewOptions()
	o.FileName = opt.FileName
	o.TempDir = opt.TempDir
	o.Threads = opt.Threads
	o.HttpOpts = loader.NewHttpOpts(opt.Url)
	o.Timeout = opt.Timeout
	o.Header = opt.headers
	j := joiner.NewJoiner(o)
	jj := new(Joiner)
	jj.jj = j
	jj.local = opt.Url == ""
	return jj
}

func (j *Joiner) loadList() string {
	if j.local {
		j.err = j.jj.LoadLocalList()
	} else {
		j.err = j.jj.LoadUrlList()
	}
	if j.err != nil {
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) loadSegments() string {
	j.err = j.jj.DownloadFiles(j.cfg)
	if j.err != nil {
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) joinSegments() string {
	j.err = j.jj.JoinFiles(j.cfg)
	if j.err != nil {
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) removeTemp() string {
	j.err = j.jj.RemoveTemp()
	if j.err != nil {
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) LoadList() string {
	j.stage = 1
	if j.loadList() != "" {
		j.stage = -1
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) Load() {
	go func() {
		j.loading = true
		defer func() { j.loading = false }()
		j.stoped = false
		j.stage = 2
		if j.loadSegments() != "" {
			j.stage = -1
			return
		}
		if j.stoped {
			j.stage = 0
			return
		}
		j.stage = 3
		if j.joinSegments() != "" {
			j.stage = -1
			return
		}
		if j.stoped {
			j.stage = 0
			return
		}
		j.stage = 4
		if j.removeTemp() != "" {
			j.stage = -1
			return
		}
		j.stage = 5
	}()
}

func (j *Joiner) Loaded() int {
	c, _ := j.jj.GetLoaded()
	return c
}

func (j *Joiner) LoadedCount() int {
	_, c := j.jj.GetLoaded()
	return c
}

func (j *Joiner) IsLoading() bool {
	return j.loading
}

func (j *Joiner) GetStage() int {
	return j.stage
}

func (j *Joiner) GetError() string {
	if j.err != nil {
		return j.err.Error()
	}
	return ""
}

func (j *Joiner) GetListCount() int {
	if j.jj.GetList() != nil {
		return len(j.jj.GetList().Lists)
	}
	return -1
}

func (j *Joiner) GetListUrl(i int) string {
	list := j.jj.GetList()
	if list != nil && i >= 0 && i < len(list.Lists) {
		return list.Lists[i].Url
	}
	return ""
}

func (j *Joiner) SetListLoad(i int) {
	if i == -1 {
		j.cfg = nil
	} else {
		if j.jj.GetList() != nil && len(j.jj.GetList().Lists) > 0 {
			if j.cfg == nil {
				j.cfg = make(map[int]bool)
			}
			j.cfg[i] = true
		}
	}
}

func (j *Joiner) Stop() {
	j.stoped = true
	j.jj.Stop()
	j.stage = 0
}

func (j *Joiner) IsFinish() bool {
	return j.jj.IsFinish()
}

func RemoveDir(dir string) string {
	err := os.RemoveAll(dir)
	if err != nil {
		return err.Error()
	}
	return ""
}
