package joiner

import (
	"io"
	"loader"
	"m3u8"
	"os"
	"path/filepath"
)

func getWorkers(list *m3u8.List, tempSubDir string, opt *Options) []*Worker {
	ret := make([]*Worker, 0)
	for _, c := range list.Content {
		segName := getTempFilename(opt.TempDir, c, tempSubDir)
		st := stat(segName)
		if st == nil || st.Size() == 0 {
			ret = append(ret, NewWorker(opt, c, segName))
		}
	}
	return ret
}

func getTempFilename(tmpDir, fileName, subTmpDir string) string {
	if subTmpDir == "" {
		return filepath.Join(tmpDir, filepath.Base(fileName))
	}
	return filepath.Join(tmpDir, subTmpDir, filepath.Base(fileName))
}

func exists(path string) bool {
	_, err := os.Stat(path)
	return !os.IsNotExist(err)
}

func stat(path string) os.FileInfo {
	i, _ := os.Stat(path)
	return i
}

func loadUrl(opt *loader.HttpOpts) ([]byte, error) {
	http := loader.NewHttp(opt)
	err := http.Connect()
	if err != nil {
		return nil, err
	}
	tmpbuf := make([]byte, 1024*32)
	buf := make([]byte, 0)
	err = nil
	n := 0
	for err == nil {
		n, err = http.Read(tmpbuf)
		if n > 0 {
			buf = append(buf, tmpbuf[:n]...)
		}
	}

	if err == io.EOF {
		err = nil
	}

	return buf, err
}
