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
	Stage_Finished
	Stage_LoadingList
	Stage_LoadingContent
	Stage_JoinSegments
	Stage_RemoveTemp
)

type M3U8 struct {
	list *List
	opt  *Options

	lastErr error

	isLoading bool
	isJoin    bool

	loadIndex int
	loadCount int

	state       chan *State
	stateMutext sync.Mutex
}

func NewM3U8(opt *Options) *M3U8 {
	m := new(M3U8)
	m.opt = opt
	return m
}

func (m *M3U8) LoadList() error {
	m.sendState(0, 0, Stage_LoadingList, m.opt.Url, nil)
	m.isLoading = true
	defer func() { m.isLoading = false }()
	local := strings.HasPrefix(strings.ToLower(m.opt.Url), "file://")
	ho := m.opt.HttpOpts
	var list *List
	var err error
	if local {
		list, err = ParseLocalList(ho)
	} else {
		list, err = ParseList(ho)
	}
	if err != nil {
		m.errors(err)
		return err
	}
	list.Name = m.opt.Name
	list.Item.FilePath = filepath.Join(m.opt.TempDir, list.Name)
	list.IsLoad = true
	m.loadCount = m.prepareList(list)
	m.list = list
	m.SaveList()
	return nil
}

func (m *M3U8) prepareList(l *List) int {
	count := 0
	for i, n := range l.Items {
		filename := filepath.Base(n.Url)
		if len(filename)+len(l.FilePath) > 260 {
			filename = fmt.Sprintf("segment_%d.ts", i)
		}
		n.FilePath = filepath.Join(l.FilePath, filename)
		n.IsLoad = true
		count++
	}
	for i, sl := range l.Lists {
		sl.IsLoad = true
		if sl.Name == "" {
			sl.Name = fmt.Sprintf("%s.%d.%d", l.Name, i+1, sl.Bandwidth)
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
		count += len(l.Items)
		for _, l := range l.Lists {
			if l.IsLoad {
				walk(l)
			}
		}
	}
	walk(m.list)
	return count
}

func (m *M3U8) GetList() *List {
	if m.list == nil {
		m.list, _ = LoadList(filepath.Join(m.opt.TempDir, m.opt.Name) + ".lst")
	}
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
	m.sendState(0, 0, Stage_Stoped, "", m.lastErr)
}

func (m *M3U8) errors(err error) {
	m.isJoin = false
	m.isLoading = false
	if err != nil {
		m.lastErr = err
	}
	m.sendState(0, 0, Stage_Error, "", m.lastErr)
}

func (m *M3U8) Finish() {
	m.isJoin = false
	m.isLoading = false
	m.sendState(0, 0, Stage_Finished, "", m.lastErr)
}
