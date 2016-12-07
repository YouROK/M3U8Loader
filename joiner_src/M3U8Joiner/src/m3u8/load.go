package m3u8

import (
	//	"io/ioutil"
	"io"
	"loader"
	"os"
	"path/filepath"
	"sync"

	"fmt"
)

func (m *M3U8) Load() error {
	m.isLoading = true
	err := m.load(m.GetCount())
	m.isLoading = false
	if err == nil {
		m.isFinish = true
	}
	m.lastErr = err
	m.Stop()
	return err
}

func (m *M3U8) load(count int) error {
	var wg sync.WaitGroup
	var mut sync.Mutex
	var err error
	pos := 0
	for i := 0; i < m.opt.Threads; i++ {
		wg.Add(1)
		go func() {
			for m.isLoading {
				var item *Item
				mut.Lock()
				item = m.getItem(pos)
				if item != nil {
					pos++
					m.sendState(pos, count, LoadingContent, item.Url, nil)
				}
				m.loadIndex = pos
				mut.Unlock()
				if item == nil {
					break
				}
				e := m.loadItem(item)
				if e != nil {
					err = e
					m.Stop()
					break
				}
			}
			wg.Done()
		}()
	}
	wg.Wait()
	return err
}

func (m *M3U8) loadItem(item *Item) error {
	if item != nil && !item.IsLoad {
		return nil
	}
	if item == nil {
		fmt.Println("*** Warn item is nil ***")
		return nil
	}
	if stat(item.FilePath) > 0 {
		return nil
	}
	opt := *m.opt.HttpOpts
	opt.Url = item.Url
	http := loader.NewHttp(&opt)
	err := http.Connect()
	if err != nil {
		return err
	}
	defer http.Close()
	if err == nil {
		if !exists(filepath.Dir(item.FilePath)) {
			os.MkdirAll(filepath.Dir(item.FilePath), 0777)
		}

		tmpBuf := make([]byte, 1024)
		n := 0
		f, err := os.Create(item.FilePath)
		if err != nil {
			return err
		}
		defer f.Close()
		for err == nil && m.isLoading {
			n, err = http.Read(tmpBuf)
			if n > 0 {
				_, err = f.Write(tmpBuf[:n])
			}
		}
	}

	if err != io.EOF && (err != nil || !m.isLoading) {
		os.Remove(item.FilePath)
	}

	if err == io.EOF {
		err = nil
	}

	return err
}

func (m *M3U8) getItem(pos int) *Item {
	ind := 0
	var walk func(l *List, ind *int) *Item

	walk = func(l *List, ind *int) *Item {
		for _, i := range l.items {
			if *ind == pos {
				return i
			}
			*ind++
		}
		for _, l := range l.lists {
			if l.IsLoad {
				i := walk(l, ind)
				if i != nil {
					return i
				}
			}
		}
		return nil
	}
	return walk(m.list, &ind)
}
