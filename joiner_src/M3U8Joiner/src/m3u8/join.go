package m3u8

import (
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"fmt"
)

func (m *M3U8) Join() error {
	m.isJoin = true
	err := m.join(m.list)
	m.isJoin = false
	m.lastErr = err
	return err
}

func (m *M3U8) join(l *List) error {
	if len(l.items) > 0 {
		filename := filepath.Join(m.opt.OutFileDir, l.Name+".mp4")
		file, err := os.Create(filename)
		if err != nil {
			return err
		}
		defer file.Close()
		isTs := false
		fmt.Println("Join:", filename)
		for _, i := range l.items {
			if !i.IsLoad {
				continue
			}
			fmt.Println("add", i.FilePath)
			if strings.ToLower(filepath.Ext(i.FilePath)) == ".ts" {
				buf, err := ioutil.ReadFile(i.FilePath)
				if err != nil {
					return err
				}
				_, err = file.Write(buf)
				if err != nil {
					return err
				}
				file.Sync()
				isTs = true
			} else {
				fmt.Println("move", i.FilePath)
				err := os.Rename(i.FilePath, filepath.Join(m.opt.OutFileDir, filepath.Base(i.FilePath)))
				if err != nil {
					return err
				}
			}
			if !m.isJoin {
				return nil
			}
		}

		if !isTs {
			os.Remove(filename)
		}
	}
	for _, i := range l.lists {
		if !i.IsLoad {
			continue
		}
		err := m.join(i)
		if err != nil {
			return err
		}
		if !m.isJoin {
			return nil
		}
	}
	return nil
}
