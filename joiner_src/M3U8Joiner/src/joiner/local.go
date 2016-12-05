package joiner

import (
	"encoding/json"
	"io/ioutil"
	"m3u8"
	"os"
	"path/filepath"
)

func (j *Joiner) writeLocalList() error {
	if !exists(j.opt.TempDir) {
		err := os.MkdirAll(j.opt.TempDir, 0777)
		if err != nil && !os.IsExist(err) {
			return err
		}
	}
	filename := filepath.Join(j.opt.TempDir, filepath.Base(j.opt.FileName)+".cfg")
	buf, err := json.Marshal(j.list)
	if err != nil {
		return err
	}
	return ioutil.WriteFile(filename, buf, 0666)
}

func (j *Joiner) readLocalList() error {
	filename := filepath.Join(j.opt.TempDir, filepath.Base(j.opt.FileName)+".cfg")
	if !exists(filename) {
		return os.ErrNotExist
	}
	buf, err := ioutil.ReadFile(filename)
	if err != nil {
		return err
	}
	j.list = new(m3u8.List)
	err = json.Unmarshal(buf, j.list)
	if err != nil {
		return err
	}
	return nil
}
