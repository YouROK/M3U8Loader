package m3u8

import (
	"fmt"
	"path/filepath"
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
}

func NewM3U8(opt *Options) *M3U8 {
	m := new(M3U8)
	m.opt = opt
	return m
}

func (m *M3U8) LoadListNet() error {
	ho := m.opt.HttpOpts
	list, err := ParseList(ho)
	m.lastErr = err
	if err != nil {
		return err
	}
	list.Name = m.opt.Name
	list.Item.FilePath = filepath.Join(m.opt.TempDir, list.Name)
	m.loadCount = m.prepareList(list)
	m.list = list
	m.isFinish = false
	m.isLoading = false
	return nil
}

func (m *M3U8) prepareList(l *List) int {
	count := 0
	for _, n := range l.items {
		n.FilePath = filepath.Join(l.FilePath, l.Name, filepath.Base(n.Url))
		n.IsLoad = true
		count++
	}
	for i, sl := range l.lists {
		sl.Item.FilePath = filepath.Join(l.Item.FilePath, sl.Name)
		if sl.Name == "" {
			sl.Name = fmt.Sprintf("%s.%d", l.Name, i+1)
		}
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
