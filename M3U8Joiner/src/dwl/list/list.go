package list

import (
	"dwl/client"
	"dwl/crypto"
	"dwl/stats"
	"dwl/utils"
	"errors"
	"fmt"
	"io"
	"net/http"
)

var Error_TextData = errors.New("loading not media data")

type List struct {
	Items  []*stats.Item
	EncKey *crypto.Key `json:",omitempty"`

	Url       string
	Name      string
	Bandwidth int
	Headers   http.Header
	Subtitles string
}

func (l *List) Get(index int) *stats.Item {
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
		return l.load(itm, l.Headers)
	}
	return nil
}

func (l *List) Stop() {
	for _, i := range l.Items {
		i.SetLoading(false)
	}
}

func (l *List) Range(fn func(i int, itm *stats.Item)) {
	for i, itm := range l.Items {
		fn(i, itm)
	}
}

func (l *List) load(itm *stats.Item, header http.Header) error {
	if itm.IsLoading() || itm.IsLoadComplete() {
		return nil
	}
	itm.SetLoading(true)
	itm.SetLoadComplete(false)
	itm.CleanBuffer()
	defer func() {
		itm.SetLoading(false)
	}()

	cli, err := client.GetClient(itm.Url, header)
	if err != nil {
		itm.SetError(err)
		return err
	}

	err = cli.Connect()
	if err != nil {
		itm.SetError(err)
		return err
	}

	defer func() {
		cli.Close()
		cli = nil
	}()

	buffer := make([]byte, 32768)
	n := 0
	itm.Size = cli.GetSize()
	if itm.Size <= 0 {
		return errors.New("get wrong size: " + itm.Url + " 0 bytes")
	}
	itm.InitBuffer(itm.Size)
	itm.StartSpeed()
	for err == nil && itm.IsLoading() {
		n, err = cli.Read(buffer)
		itm.MeasureSpeed(n)
		if n > 0 {
			if len(itm.GetBuffer()) == 0 {
				if utils.IsBinarySafe(buffer[:n]) < 2 {
					fmt.Println("Error loaded page than media", string(buffer))
					err = Error_TextData
					break
				}
			}
			if n > 0 {
				itm.AppendBuffer(buffer[:n])
			}
		}
	}
	itm.StopSpeed()
	if err == io.EOF {
		err = nil
		itm.SetLoadComplete(true)
	}
	if err != nil {
		itm.Size = 0
		itm.CleanBuffer()
	}
	itm.SetError(err)
	return err
}
