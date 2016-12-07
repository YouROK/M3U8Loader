package m3u8

import (
	"fmt"
	"path/filepath"
	"strings"
	"sync"
)

const (
	Stoped = iota
	LoadingList
	LoadingContent
	JoinSegments
)

type M3U8 struct {
	list *List
	opt  *Options

	lastErr error

	isFinish  bool
	isLoading bool
	isJoin    bool

	loadIndex int
	loadCount int

	stateChan   chan State
	stateMutext sync.Mutex
}

func NewM3U8(opt *Options) *M3U8 {
	m := new(M3U8)
	m.opt = opt
	return m
}

func (m *M3U8) LoadList() error {
	local := strings.HasPrefix(strings.ToLower(m.opt.Url), "file://")

	ho := m.opt.HttpOpts
	m.sendState(0, 0, LoadingList, ho.Url, nil)
	var list *List
	var err error
	if local {
		list, err = ParseLocalList(ho)
	} else {
		list, err = ParseList(ho)
	}
	m.lastErr = err
	if err != nil {
		m.sendState(0, 0, Stoped, ho.Url, err)
		return err
	}
	list.Name = m.opt.Name
	list.Item.FilePath = filepath.Join(m.opt.TempDir, list.Name)
	m.loadCount = m.prepareList(list)
	m.list = list
	m.Stop()
	return nil
}

func (m *M3U8) prepareList(l *List) int {
	count := 0
	for _, n := range l.items {
		n.FilePath = filepath.Join(l.FilePath, filepath.Base(n.Url))
		n.IsLoad = true
		count++
	}
	for i, sl := range l.lists {
		sl.IsLoad = true
		if sl.Name == "" {
			sl.Name = fmt.Sprintf("%s.%d", l.Name, i+1)
		}
		sl.Item.FilePath = filepath.Join(l.Item.FilePath, sl.Name)
		count += m.prepareList(sl)
	}
	return count
}

func (m *M3U8) GetCount() int {
	count := 0
	var walk func(l *List)

	walk = func(l *List) {
		count += len(l.items)
		for _, l := range l.lists {
			if l.IsLoad {
				walk(l)
			}
		}
	}
	walk(m.list)
	return count
}

func (m *M3U8) GetList() *List {
	return m.list
}

func (m *M3U8) Stop() {
	m.isJoin = false
	m.isLoading = false
	m.sendState(0, 0, Stoped, "", m.lastErr)
}
