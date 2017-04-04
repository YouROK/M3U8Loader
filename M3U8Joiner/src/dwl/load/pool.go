package load

import (
	"dwl/list"
	"dwl/settings"
	"sync"
	"time"
)

type Pool struct {
	workers []*Worker
	list    *list.List
	sets    *settings.Settings
	mut     sync.Mutex
	working bool
	wait    chan bool
	err     error
}

type Worker struct {
	index  int
	list   *list.List
	sets   *settings.Settings
	iswork bool
	status int
	err    error
	update func()
}

func NewPool(sets *settings.Settings, list *list.List) *Pool {
	p := new(Pool)
	p.sets = sets
	p.list = list
	p.wait = make(chan bool, 1)
	return p
}

func NewWorker(i int, update func(), list *list.List, sets *settings.Settings) *Worker {
	w := new(Worker)
	w.update = update
	w.list = list
	w.sets = sets
	w.index = i
	return w
}

func (p *Pool) Push(itm int, update func()) bool {
	if itm == -1 {
		return false
	}
	p.mut.Lock()
	defer p.mut.Unlock()
	if len(p.workers) >= p.sets.Threads {
		return false
	}
	for _, w := range p.workers {
		if w.index == itm {
			return false
		}
	}
	p.workers = append(p.workers, NewWorker(itm, update, p.list, p.sets))
	return true
}

func (p *Pool) Len() int {
	return len(p.workers)
}

func (p *Pool) Start() {
	p.mut.Lock()
	if p.working {
		p.mut.Unlock()
		return
	}
	p.working = true
	p.err = nil
	p.workers = make([]*Worker, 0)
	p.mut.Unlock()

	go func() {
		done := make(chan struct{}, 64)
		for p.working {
			p.mut.Lock()
			for i := 0; i < len(p.workers); i++ {
				w := p.workers[i]
				if w.status == STATUS_ERROR {
					p.err = w.err
					p.working = false
					break
				} else if w.status == STATUS_COMPLETE {
					p.workers = append(p.workers[:i], p.workers[i+1:]...)
					i--
					continue
				} else if w.status == STATUS_STOPED && p.working {
					go func() {
						w.start()
						done <- struct{}{}
					}()
				}
			}
			p.mut.Unlock()
			<-done
		}
		p.wait <- true
	}()
}

func (p *Pool) Stop() {
	p.working = false
	p.mut.Lock()
	defer p.mut.Unlock()
	for _, w := range p.workers {
		w.stop()
	}
	<-p.wait
}

func (p *Pool) Error() error {
	return p.err
}

func (w *Worker) start() {
	w.status = STATUS_LOADING
	w.err = nil
	for i := 0; i < w.sets.ErrorRepeat && w.status == STATUS_LOADING; i++ {
		w.err = w.list.Load(w.index)
		if w.err == nil {
			w.status = STATUS_COMPLETE
			break
		}
		wait := time.Millisecond * 200 * time.Duration(i+1)
		if wait > time.Second {
			wait = time.Second
		}
		time.Sleep(wait)
	}
	if w.err != nil {
		w.status = STATUS_ERROR
	}
	if w.update != nil {
		w.update()
	}
}

func (w *Worker) stop() {
	if w.status != STATUS_COMPLETE {
		w.status = STATUS_STOPED
	}
	if itm := w.list.Get(w.index); itm != nil {
		itm.IsLoading = false
	}
}
