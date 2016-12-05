package joiner

import (
	"io"
	"io/ioutil"
	"loader"
	//	"net/url"
	"os"
	"path/filepath"
	//	 "strings"

	//	"fmt"
)

type Worker struct {
	FileName string
	Url      string
	Err      error
	End      bool
	Size     int64

	opt      *Options
	finished bool

	OnError func(*Worker)
	OnEnd   func(*Worker)
}

func NewWorker(opt *Options, url, filePath string) *Worker {
	w := new(Worker)
	w.opt = opt
	w.Url = url
	w.FileName = filePath
	return w
}

func (w *Worker) DoStop() {
	w.finished = true
}

func (w *Worker) DoWork() {
	w.finished = false
	tmpBuf := make([]byte, 1024*32)
	n := 0

	opt := new(loader.HttpOpts)
	opt.Url = w.Url
	opt.Header = w.opt.Header

	if !exists(filepath.Dir(w.FileName)) {
		os.MkdirAll(filepath.Dir(w.FileName), 0777)
	}
	http := loader.NewHttp(opt)
	//	fmt.Println("Connect", w.Url)
	w.Err = http.Connect()
	if w.Err == nil {
		Buffer := make([]byte, 0)
		defer http.Close()
		w.Size = 0
		for w.Err == nil || w.Err == io.EOF {
			n, w.Err = http.Read(tmpBuf)
			if n > 0 {
				Buffer = append(Buffer, tmpBuf[:n]...)
				w.Size += int64(n)
			}
			if w.Err == io.EOF {
				w.Err = nil
				break
			}
			if w.finished {
				return
			}
		}
		if w.Err == nil {
			w.Err = ioutil.WriteFile(w.FileName, Buffer, 0666)
		}
	}

	if w.Err != nil {
		w.sendError()
	} else {
		w.End = true
		if w.OnEnd != nil {
			w.OnEnd(w)
		}
	}
}

func (w *Worker) sendError() {
	if w.OnError != nil {
		w.OnError(w)
	}
}
