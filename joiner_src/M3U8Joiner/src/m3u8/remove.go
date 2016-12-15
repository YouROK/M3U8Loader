package m3u8

import (
	"os"
	"path/filepath"
)

func (m *M3U8) RemoveTemp() error {
	remDir := filepath.Join(m.opt.TempDir, m.opt.Name)
	m.sendState(0, 0, Stage_RemoveTemp, remDir, nil)
	err := os.RemoveAll(remDir)
	if err != nil {
		m.errors(err)
	}
	return err
}

func RemoveAll(dir string) error {
	return os.RemoveAll(dir)
}
