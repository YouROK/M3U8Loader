package file

import (
	"errors"
	"net/http"
	"net/url"
	"os"
	"sync"
)

type File struct {
	url string

	file  *os.File
	mutex sync.Mutex
	err   error
}

func NewFile(url string) *File {
	con := new(File)
	con.url = url
	return con
}

func (c *File) Connect() error {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	url, err := url.Parse(c.url)
	if err != nil {
		c.err = err
		return err
	}
	c.file, c.err = os.Open(url.Path)
	return c.err
}

func (c *File) IsConnected() bool {
	return c.file != nil
}

func (c *File) GetLastError() error {
	return c.err
}

func (c *File) Read(buf []byte) (n int, err error) {
	c.mutex.Lock()
	defer c.mutex.Unlock()
	if c.file != nil {
		n, err = c.file.Read(buf)
		c.err = err
		return
	}
	c.err = errors.New("file: invalid Read on closed file")
	return 0, c.err
}

func (c *File) Close() error {
	if c == nil {
		return nil
	}

	c.mutex.Lock()
	defer c.mutex.Unlock()
	if c.file != nil {
		c.err = c.file.Close()
	}
	c.file = nil
	return c.err
}

func (c *File) GetHeader(key string) string {
	return ""
}

func (c *File) GetHeaders() http.Header {
	return nil
}

func (c *File) GetSize() int64 {
	if c.file != nil {
		i, _ := c.file.Stat()
		if i != nil {
			return i.Size()
		}
	}
	return -1
}
