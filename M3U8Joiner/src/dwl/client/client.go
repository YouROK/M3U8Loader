package client

import (
	"dwl/file"
	"dwl/http"
	"errors"
	ht "net/http"
	"net/url"
)

type Client interface {
	Connect() error
	Close() error
	Read(buf []byte) (n int, err error)
	IsConnected() bool
	GetLastError() error
	GetSize() int64
}

func GetClient(Url string, header ht.Header) (Client, error) {
	url, err := url.Parse(Url)
	if err != nil {
		return nil, err
	}
	switch url.Scheme {
	case "http", "https":
		return http.NewHttp(Url, header), nil
	case "file":
		return file.NewFile(Url), nil
		//	case "test":
		//		return NewTestClient(sets, pos), nil

	default:
		return nil, errors.New("not support protocol: " + url.Scheme)
	}
}
