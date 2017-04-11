package utils

import (
	"io"
	"os"
)

func Exists(path string) bool {
	_, err := os.Stat(path)
	return !os.IsNotExist(err)
}

func WriteFileSave(filename string, data []byte) error {
	f, err := os.OpenFile(filename, os.O_WRONLY|os.O_CREATE, 0666)
	if err != nil {
		return err
	}
	n, err := f.WriteAt(data, 0)
	if err == nil && n < len(data) {
		err = io.ErrShortWrite
	}
	if info, er := f.Stat(); er == nil {
		if int64(len(data)) != info.Size() {
			f.Truncate(int64(n))
		}
	}
	f.Sync()
	if err1 := f.Close(); err == nil {
		err = err1
	}
	return err
}
