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
	if l.sets.LoadItemsSize {
		var size int64
		for _, i := range l.list.Items {
			if i.IsLoad {
				size += i.Size
			}
		}
		if size>0 {
			f.Truncate(size)
			f.Sync()
		}
	}
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

func (w *File) TruncateAuto(list *list.List, threads int) {
	w.mutex.Lock()
	defer w.mutex.Unlock()
	countItems := 0
	loadedItems := 0
	loadedBytes := int64(0)
	for _, l := range list.Items {
		if l.IsLoad {
			countItems++
			if l.IsComplete {
				loadedItems++
				loadedBytes += l.Size
			}
		}
	}
	if countItems == 0 || loadedItems < threads || loadedBytes == 0 {
		return
	}

	if countItems == loadedItems {
		w.file.Truncate(loadedBytes)
		w.file.Sync()
	} else {
		allSize := GetFileSize(90, loadedItems, countItems, loadedBytes)
		st, _ := w.file.Stat()
		if w.file != nil && st != nil && allSize > loadedBytes && allSize != st.Size() {
			prc := 100
			if st.Size() > 0 {
				prc = 100 - int(allSize*100/st.Size())
				if prc < 0 {
					prc = -prc
				}
			}
			if prc > 2 {
				w.file.Truncate(allSize)
				w.file.Sync()
			}
		}
	}
}

func (w *File) Close() error {
	if w.file != nil {
		err := w.file.Close()
		w.file = nil
		return err
	}
	return nil
}

func GetFileSize(endPerc, index, count int, bytes int64) int64 {
	if index == 0 || count == 0 || bytes == 0 {
		return 0
	}

	ret := int64(float64(count) * float64(bytes) / float64(index) * 1.5)
	return ret
}
