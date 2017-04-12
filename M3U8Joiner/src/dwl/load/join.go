package load

import (
	"dwl/crypto"
	"dwl/list"
	"dwl/utils"
	"encoding/binary"
	"io"
	"os"
	"path/filepath"
	"sync"
)

type File struct {
	file  *os.File
	mutex sync.Mutex
}

func (l *Loader) GetFilename() string {
	return filepath.Join(l.sets.DownloadPath, l.list.Name) + ".mp4"
}

func (l *Loader) openFile() (*File, error) {
	filename := l.GetFilename()
	if !utils.Exists(filepath.Dir(filename)) {
		os.MkdirAll(filepath.Dir(filename), 0777)
	}
	f, err := os.OpenFile(filename, os.O_RDWR|os.O_CREATE, 0666)
	if err != nil {
		return nil, err
	}
	ff := new(File)
	ff.file = f
	return ff, nil
}

func (w *File) WriteAt(list *list.List) error {
	w.mutex.Lock()
	defer w.mutex.Unlock()
	var offset int64
	var err error

	for i := 0; i < list.Len(); i++ {
		itm := list.Get(i)
		if itm == nil {
			break
		}

		if itm.IsLoad && itm.Size == 0 {
			break
		}

		if itm.IsLoad && itm.IsLoadComplete() && itm.GetBuffer() != nil {
			var buffer []byte = itm.GetBuffer()
			if list.EncKey != nil {
				k := *list.EncKey
				if k.IV == nil { //if iv == nil use index of item
					k.IV = make([]byte, 16)
					binary.BigEndian.PutUint32(k.IV, uint32(itm.Index))
				}
				tmp := make([]byte, len(buffer))
				copy(tmp, buffer)
				err = crypto.Decrypt(tmp, &k)
				if err != nil {
					break
				}
				buffer = tmp
			}
			var n int
			n, err = w.file.WriteAt(buffer, offset)
			if n != len(buffer) {
				err = io.ErrShortWrite
			}
			if err != nil {
				break
			}
			buffer = nil
			itm.CleanBuffer()
			itm.IsComplete = true
		}

		offset += itm.Size
	}
	w.file.Sync()
	return err
}

func (w *File) Close() error {
	if w.file != nil {
		err := w.file.Close()
		w.file = nil
		return err
	}
	return nil
}
