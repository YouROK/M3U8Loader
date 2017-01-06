package m3u8

import (
	"io"
	"io/ioutil"
	"loader"
	"os"
	"path/filepath"
	"sync"

	"fmt"
)

func (m *M3U8) Load() error {
	m.isLoading = true
	m.load(m.GetCount())
	m.isLoading = false
	if m.lastErr != nil {
		m.errors(m.lastErr)
	}
	return m.lastErr
}

func (m *M3U8) load(count int) {
	var wg sync.WaitGroup
	var mut sync.Mutex
	pos := 0
	m.cleanSpeed()

	for i := 0; i < m.opt.Threads; i++ {
		wg.Add(1)
		go func() {
			for m.isLoading {
				var item *Item
				mut.Lock()
				item = m.getItem(pos)
				if item != nil {
					pos++
					m.sendState(pos, count, Stage_LoadingContent, item.Url, nil)
				}
				m.loadIndex = pos
				mut.Unlock()
				if item == nil {
					break
				}
				err := m.loadItem(item)
				if err != nil {
					fmt.Println("Error", err, item)
					m.isLoading = false
					m.lastErr = err
					break
				}
			}
			wg.Done()
		}()
	}
	wg.Wait()
}

func (m *M3U8) loadItem(item *Item) error {
	if item != nil && !item.IsLoad {
		return nil
	}
	if item == nil {
		return nil
	}
	if stat(item.FilePath) > 0 {
		return nil
	}
	opt := *m.opt.HttpOpts
	opt.Url = item.Url
	http := loader.NewHttp(&opt)
	err := http.Connect()
	defer func() {
		http.Close()
		http = nil
	}()
	if err != nil {
		return err
	}
	buffer := make([]byte, 0)
	if err == nil {
		if !exists(filepath.Dir(item.FilePath)) {
			os.MkdirAll(filepath.Dir(item.FilePath), 0777)
		}

		tmpBuf := make([]byte, 1024)
		n := 0
		for err == nil && m.isLoading {
			n, err = http.Read(tmpBuf)
			m.messureSpeed(n)
			if n > 0 {
				buffer = append(buffer, tmpBuf[:n]...)
			}
		}
	}
	if err == io.EOF {
		err = nil
	}
	if err == nil && m.isLoading {
		err = ioutil.WriteFile(item.FilePath, buffer, 0666)
	}

	return err
}

func (m *M3U8) getItem(pos int) *Item {
	ind := 0
	var walk func(l *List, ind *int) *Item

	walk = func(l *List, ind *int) *Item {
		for _, i := range l.Items {
			if *ind == pos {
				return i
			}
			*ind++
		}
		for _, l := range l.Lists {
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
