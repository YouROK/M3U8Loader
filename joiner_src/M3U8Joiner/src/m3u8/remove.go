package m3u8

import (
	"os"
)

func (m *M3U8) RemoveTemp() error {
	var ret error
	if m.list != nil {
		for i, l := range m.list.lists {
			m.sendState(i, len(m.list.lists), Stage_RemoveTemp, l.FilePath, nil)
			err := os.RemoveAll(l.FilePath)
			if err != nil {
				m.sendState(i, len(m.list.lists), Stage_RemoveTemp, l.FilePath, err)
				ret = err
			}
		}
		for i, l := range m.list.items {
			m.sendState(i, len(m.list.lists), Stage_RemoveTemp, l.FilePath, nil)
			err := os.RemoveAll(l.FilePath)
			if err != nil {
				m.sendState(i, len(m.list.items), Stage_RemoveTemp, l.FilePath, err)
				ret = err
			}
		}
	}
	m.sendState(0, 0, Stage_Stoped, "", ret)
	return ret
}

func RemoveAll(dir string) error {
	return os.RemoveAll(dir)
}
