package stats

type Item struct {
	Index  int
	Url    string
	IsLoad bool
	err    error

	buffer []byte

	DownloadStat
}

func (i *Item) SetError(err error) {
	i.err = err
}

func (i *Item) GetError() error {
	return i.err
}

func (i *Item) InitBuffer(size int64) {
	if i.buffer != nil && int64(len(i.buffer)) < size {
		i.buffer = nil
	}
	if i.buffer == nil {
		i.buffer = make([]byte, size)
	}
	i.buffer = i.buffer[:0]
}

func (i *Item) AppendBuffer(data []byte) {
	i.buffer = append(i.buffer, data...)
}

func (i *Item) GetBuffer() []byte {
	return i.buffer
}

func (i *Item) CleanBuffer() {
	i.buffer = nil
}
