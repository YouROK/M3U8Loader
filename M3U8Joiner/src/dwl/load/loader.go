package load

import (
	"dwl/list"
	"dwl/settings"
	"errors"
	"time"
)

const (
	STATUS_STOPED = iota
	STATUS_LOADING
	STATUS_COMPLETE
	STATUS_ERROR
)

type Loader struct {
	sets *settings.Settings
	list *list.List
	file *File

	done chan bool

	isLoading bool
	err       error
}

func NewLoader(sets *settings.Settings, list *list.List) *Loader {
	l := new(Loader)
	l.list = list
	l.sets = sets
	l.done = make(chan bool, 1)
	return l
}

func (l *Loader) Load(update func(loader *Loader)) {
	if l.sets.DownloadPath == "" {
		l.err = errors.New("download path not set")
		return
	}
	if l.isLoading {
		return
	}
	l.isLoading = true
	defer func() {
		l.isLoading = false
		if l.file != nil {
			l.file.Close()
		}
		l.done <- true
	}()

	l.file, l.err = l.openFile()
	if l.err != nil {
		return
	}
	pool := NewPool(l.sets, l.list)
	pool.Start()
	for l.isLoading {
		if l.isEnd() || pool.Error() != nil {
			l.err = pool.err
			l.isLoading = false
			break
		}
		index := 0
		for pool.Len() < l.sets.Threads {
			index = l.getLoadIndex(index)
			isPush := pool.Push(index, func() {
				l.file.WriteAt(l.list)
				if update != nil {
					update(l)
				}
			})

			if !isPush {
				time.Sleep(time.Second)
				break
			}
			index++
		}
		time.Sleep(time.Millisecond * 100)
	}
	pool.Stop()
}

func (l *Loader) Stop() {
	l.isLoading = false
}

func (l *Loader) WaitLoading() {
	<-l.done
}

func (l *Loader) Complete() bool {
	return l.isEnd()
}

func (l *Loader) Error() error {
	return l.err
}

func (l *Loader) GetList() *list.List {
	return l.list
}

func (l *Loader) Status() int {
	if l.isLoading {
		return STATUS_LOADING
	} else if l.Complete() {
		return STATUS_COMPLETE
	} else if l.err != nil {
		return STATUS_ERROR
	} else {
		return STATUS_STOPED
	}
}

///////////////////////////////////////////////////////////////

func (l *Loader) getLoadIndex(startInd int) int {
	for i := startInd; i < len(l.list.Items); i++ {
		p := l.list.Get(i)
		if p.IsLoad && !p.IsLoading() && !p.IsComplete && !p.IsLoadComplete() {
			return p.Index
		}
	}
	return -1
}

func (l *Loader) isEnd() bool {
	for _, p := range l.list.Items {
		if !p.IsComplete && p.IsLoad {
			return false
		}
	}
	return true
}
