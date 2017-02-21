package m3u8

import (
	"encoding/binary"
	"errors"
	"io"
	"io/ioutil"
	"loader"
	"os"
	"path/filepath"
	"sync"
	"time"

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
				var list *List
				mut.Lock()
				item, list = m.getItem(pos)
				if item != nil {
					pos++
					m.sendState(pos, count, Stage_LoadingContent, item.Url, nil)
				}
				m.loadIndex = pos
				mut.Unlock()
				if item == nil {
					break
				}
				var err error
				for i := 0; i < 5; i++ {
					err = m.loadItem(item, list)
					if err == nil {
						break
					}
					fmt.Println("Error", err, item)
					fmt.Println("Try again", i)
					time.Sleep(time.Millisecond * 500)
				}
				if err != nil {
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

func (m *M3U8) loadItem(item *Item, list *List) error {
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

		tmpBuf := make([]byte, 1024*10)
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
		if list.EncKey != nil {
			k := *list.EncKey
			if k.IV == nil { //if iv == nil use index of item
				num := -1
				for n, i := range list.Items {
					if i == item {
						num = n + 1
						break
					}
				}
				if num == -1 {
					return errors.New("drm key is wrong")
				}
				k.IV = make([]byte, 16)
				binary.BigEndian.PutUint32(k.IV, uint32(num))
			}
			err = decrypt(buffer, &k)
			if err != nil {
				return err
			}
		}
		err = ioutil.WriteFile(item.FilePath, buffer, 0666)
	}

	return err
}

func (m *M3U8) getItem(pos int) (*Item, *List) {
	ind := 0
	var walk func(l *List, ind *int) (*Item, *List)

	walk = func(l *List, ind *int) (*Item, *List) {
		for _, i := range l.Items {
			if *ind == pos {
				return i, l
			}
			*ind++
		}
		for _, l := range l.Lists {
			if l.IsLoad {
				i, ll := walk(l, ind)
				if i != nil {
					return i, ll
				}
			}
		}
		return nil, nil
	}
	return walk(m.list, &ind)
}
