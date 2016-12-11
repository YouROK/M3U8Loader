package m3u8

import (
	"fmt"
	"path/filepath"
	"strings"
	"sync"
)

const (
	Stage_Stoped = iota
	Stage_Error
	Stage_LoadingList
	Stage_LoadingContent
	Stage_JoinSegments
	Stage_RemoveTemp
	Stage_Finished
)

type M3U8 struct {
	list *List
	opt  *Options

	lastErr error

	isLoading bool
	isJoin    bool

	loadIndex int
	loadCount int

	state       *State
	stateMutext sync.Mutex
}

func NewM3U8(opt *Options) *M3U8 {
	m := new(M3U8)
	m.opt = opt
	return m
}

func (m *M3U8) LoadList() error {
	m.isLoading = true
	defer func() { m.isLoading = false }()
	local := strings.HasPrefix(strings.ToLower(m.opt.Url), "file://")
	ho := m.opt.HttpOpts
	m.sendState(0, 0, Stage_LoadingList, ho.Url, nil)
	var list *List
	var err error
	if local {
		list, err = ParseLocalList(ho)
	} else {
		list, err = ParseList(ho)
	}
	m.lastErr = err
	if err != nil {
		m.sendState(0, 0, Stage_Error, ho.Url, err)
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
	for i, n := range l.items {
		filename := filepath.Base(n.Url)
		if len(filename)+len(l.FilePath) > 260 {
			filename = fmt.Sprintf("segment_%d.ts", i)
		}
		n.FilePath = filepath.Join(l.FilePath, filename)
		n.IsLoad = true
		count++
	}
	for i, sl := range l.lists {
		sl.IsLoad = true
		if sl.Name == "" {
			sl.Name = fmt.Sprintf("%s.%d.%d", l.Name, i+1, sl.bandwidt)
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

func (m *M3U8) IsLoading() bool {
	return m.isLoading
}

func (m *M3U8) IsJoin() bool {
	return m.isJoin
}

func (m *M3U8) Stop() {
	m.isJoin = false
	m.isLoading = false
	if m.lastErr != nil {
		m.sendState(0, 0, Stage_Error, "", m.lastErr)
	} else {
		m.sendState(0, 0, Stage_Stoped, "", nil)
	}
}
