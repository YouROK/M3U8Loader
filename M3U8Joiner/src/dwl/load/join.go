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

func (w *File) Truncate(list *list.List) {
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
	if countItems == 0 || loadedItems < 9 || loadedBytes == 0 {
		return
	}
	allSize := GetFileSize(90, loadedItems, countItems, loadedBytes)
	st, _ := w.file.Stat()
	if w.file != nil && st != nil && allSize > loadedBytes && allSize != st.Size() {
		w.file.Truncate(allSize)
		w.file.Sync()
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

//Высчитываем приблизительный размер файла
//так как при открытии недокаченного файла в плеере, он играет до того момента, на сколько он был скачан при открытии
//пример, файл на момент открытия в плеере был закача на 10% и приблизительно 10 минут, плеер проиграет 10 минут и закончит воспроизведение,
//а файл в этот момент может уже скачатся, по этому нужно заранее задать размер файла и чтобы он был не меньше оригинала
//Алгоритм:
//0 - 70% прибавляем проценты к приблизительному размеру
//70 - 100% считаем как есть
//
//1 вычисляем сколько файлов в 70% и берем его за конечную точку
//2 далее вычисляем процент скачанных(N) и вычисляем сколько нужно прибавить (100-N)
//в начале скачки, файл будет в 2 раза больше

func GetFileSize(endPerc, index, count int, bytes int64) int64 {
	if index == 0 || count == 0 || bytes == 0 {
		return 0
	}
	fakeEnd := (count / 100) * endPerc
	if index >= fakeEnd {
		return int64((float64(count) / float64(index) * float64(bytes)) + .5)
	}

	tmp := float64(index*100) / float64(fakeEnd)
	tmp = 100 - tmp
	return int64((float64(count) / float64(index) * (float64(bytes) + (float64(bytes) / float64(100) * float64(tmp)))) + .5)
}
