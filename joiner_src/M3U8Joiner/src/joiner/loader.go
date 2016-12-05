package joiner

import (
	"fmt"
	"m3u8"
	"sync"
)

type loaderList struct {
	list    *m3u8.List
	load    map[int]bool
	opt     *Options
	workers []*Worker
	finish  bool
	err     error
}

func newLoader(list *m3u8.List, load map[int]bool, opt *Options) *loaderList {
	l := new(loaderList)
	l.list = list
	l.load = load
	l.opt = opt
	return l
}

func (l *loaderList) download(onEnd func(*Worker)) error {
	l.finish = false
	var wg sync.WaitGroup
	var m sync.Mutex

	l.workers = make([]*Worker, 0)
	if len(l.list.Content) > 0 {
		l.workers = getWorkers(l.list, "", l.opt)
	}
	if len(l.list.Lists) > 0 {
		for i := 0; i < len(l.list.Lists); i++ {
			if l.load == nil {
				subtmp := fmt.Sprint(l.list.Lists[i].Bandwidth)
				tmp := getWorkers(l.list.Lists[i], subtmp, l.opt)
				l.workers = append(l.workers, tmp...)
			} else if ok, b := l.load[i]; ok && b {
				subtmp := fmt.Sprint(l.list.Lists[i].Bandwidth)
				tmp := getWorkers(l.list.Lists[i], subtmp, l.opt)
				l.workers = append(l.workers, tmp...)
			}
		}
	}
	fmt.Println("Workers:", len(l.workers))
	pos := 0
	if len(l.workers) > 0 {
		for i := 0; i < l.opt.Threads; i++ {
			wg.Add(1)
			go func(t int) {
				fmt.Println("Start thread", t)
				for !l.finish {
					var worker *Worker
					m.Lock()
					if pos < len(l.workers) {
						worker = l.workers[pos]
					}
					pos++
					m.Unlock()
					if pos > len(l.workers) || l.finish {
						break
					}
					worker.OnError = l.onErrWorker
					worker.OnEnd = onEnd
					worker.DoWork()
				}
				wg.Done()
			}(i)
		}
		wg.Wait()
	}
	l.finish = true
	return l.err
}

func (l *loaderList) stop() {
	l.finish = true
}

func (l *loaderList) size() int {
	return len(l.workers)
}

func (l *loaderList) loaded() int {
	size := 0
	for _, w := range l.workers {
		if w.End {
			size++
		}
	}
	return size
}

func (l *loaderList) onErrWorker(w *Worker) {
	l.err = w.Err
	l.stop()
}
