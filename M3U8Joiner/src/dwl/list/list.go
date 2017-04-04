package list

import (
	"dwl/client"
	"dwl/crypto"
	"dwl/progress"
	"io"
	"net/http"
	"sync"
	"time"
)

type List struct {
	Items  []*Item     `json:",omitempty"`
	EncKey *crypto.Key `json:",omitempty"`

	Url     string
	Name    string
	Headers http.Header

	Bandwidth int
}

type Item struct {
	Index  int
	Url    string
	IsLoad bool
	Size   int64
	Offset int64

	progress.DownloadProgress

	wait sync.WaitGroup
	mut  sync.Mutex
	err  error

	Buffer       []byte `json:"-"`
	IsFinishLoad bool   `json:"-"`
}

func (l *List) Get(index int) *Item {
	for _, i := range l.Items {
		if i.Index == index {
			return i
		}
	}
	return nil
}

func (l *List) Len() int {
	return len(l.Items)
}

func (l *List) Load(index int) error {
	itm := l.Get(index)
	if itm != nil && itm.IsLoad {
		return itm.load(l.Headers)
	}
	return nil
}

func (l *List) Stop() {
	for _, i := range l.Items {
		if i.IsLoading {
			i.IsLoading = false
		}
	}
}

func (l *List) Range(fn func(i int, itm *Item)) {
	for i, itm := range l.Items {
		fn(i, itm)
	}
}

func (i *Item) load(header http.Header) error {
	i.mut.Lock()
	defer i.mut.Unlock()
	if i.IsLoading || i.IsFinishLoad {
		return nil
	}
	i.IsLoading = true
	i.IsFinishLoad = false
	i.Buffer = nil
	i.wait.Add(1)
	defer func() {
		i.IsLoading = false
		i.wait.Done()
	}()

	startTime := time.Now()
	cli, err := client.GetClient(i.Url, header)
	if err != nil {
		i.err = err
		return err
	}

	err = cli.Connect()
	if err != nil {
		i.err = err
		return err
	}

	defer func() {
		if i.IsFinishLoad {
			i.LoadingTime = time.Since(startTime)
		}
		cli.Close()
		cli = nil
	}()

	buffer := make([]byte, 32768)
	n := 0
	i.Size = 0
	i.ConnectTime = time.Since(startTime)
	i.StartSpeed()
	for err == nil && i.IsLoading {
		n, err = cli.Read(buffer)
		i.MessureSpeed(n)
		if n > 0 {
			i.Buffer = append(i.Buffer, buffer[:n]...)
		}
		i.Size = int64(len(i.Buffer))
	}
	i.StopSpeed()
	if err == io.EOF {
		err = nil
		i.IsFinishLoad = true
	}
	if err != nil {
		i.Size = 0
		i.AverSpeed = 0
	}
	i.err = err
	return err
}

func (i *Item) Err() error {
	return i.err
}

func (i *Item) LeftBytes() int64 {
	return i.Size - i.LoadedByte
}
