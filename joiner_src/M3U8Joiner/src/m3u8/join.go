package m3u8

import (
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"syscall"
	"time"
)

func (m *M3U8) Join() error {
	m.isJoin = true
	err := m.join(m.list)
	m.isJoin = false
	if err != nil {
		m.errors(err)
	}
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
		for z, i := range l.items {
			if !i.IsLoad {
				continue
			}
			m.sendState(z+1, len(l.items), Stage_JoinSegments, filename, nil)
			if strings.ToLower(filepath.Ext(i.FilePath)) == ".ts" {
				buf, err := ioutil.ReadFile(i.FilePath)
				if err != nil {
					//Issue: on open eintr
					if isEintrErr(err) {
						for k := 0; k < 50; k++ {
							buf, err = ioutil.ReadFile(i.FilePath)
							if !isEintrErr(err) {
								break
							}
							time.Sleep(time.Microsecond * 100)
						}
					}

					if err != nil {
						return err
					}
				}
				_, err = file.Write(buf)
				if err != nil {
					return err
				}
				file.Sync()
				isTs = true
			} else {
				m.sendState(z+1, len(l.items), Stage_JoinSegments, filepath.Base(i.FilePath), nil)
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

func isEintrErr(err error) bool {
	if err == nil {
		return false
	}
	if e, ok := err.(*os.PathError); ok && e.Err == syscall.EINTR {
		return true
	}

	if e, ok := err.(syscall.Errno); ok && e == syscall.EINTR {
		return true
	}

	return false
}
