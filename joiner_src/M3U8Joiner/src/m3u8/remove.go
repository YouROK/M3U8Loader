package m3u8

import (
	"os"
	"path/filepath"
)

func (m *M3U8) RemoveTemp() error {
	m.sendState(0, 0, Stage_RemoveTemp, m.opt.TempDir, nil)
	err := os.RemoveAll(filepath.Join(m.opt.TempDir, m.opt.Name))
	m.sendState(0, 0, Stage_Finished, "", err)
	return err
}

func RemoveAll(dir string) error {
	return os.RemoveAll(dir)
}
