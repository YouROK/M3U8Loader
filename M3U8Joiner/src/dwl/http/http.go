package http

import (
	"errors"
	"net/http"
	"runtime"
	"strconv"
	"strings"
	"sync"
)

type Http struct {
	client *http.Client
	req    *http.Request
	resp   *http.Response

	url    string
	header http.Header

	mutex sync.Mutex
	err   error
}

func NewHttp(url string, header http.Header) *Http {
	con := new(Http)
	con.url = url
	if len(header) > 0 {
		con.header = header
	}
	return con
}

func (h *Http) Connect() error {
	h.mutex.Lock()
	h.client = &http.Client{}
	h.req, h.err = http.NewRequest("GET", h.url, nil)
	h.mutex.Unlock()
	if h.err != nil {
		defer h.Close()
		return h.err
	}

	if h.header != nil {
		h.req.Header = (http.Header)(h.header)
	}

	if h.req.Header.Get("UserAgent") == "" {
		h.req.Header.Set("UserAgent", "DWL/1.0.0 ("+runtime.GOOS+")")
	}

	if h.req.Header.Get("Accept") == "" {
		h.req.Header.Set("Accept", "*/*")
	}

	if h.req.Header.Get("Accept-Encoding") == "" {
		h.req.Header.Set("Accept-Encoding", "identity")
	}

	h.mutex.Lock()
	h.resp, h.err = h.client.Do(h.req)
	if h.resp != nil && (h.resp.StatusCode != http.StatusOK && h.resp.StatusCode != http.StatusPartialContent) {
		h.err = errors.New(h.resp.Request.URL.String() + " " + strconv.Itoa(h.resp.StatusCode) + " " + h.resp.Status)
	}
	h.mutex.Unlock()

	if h.err != nil {
		h.Close()
		return h.err
	}
	if h.resp != nil && h.resp.Request != nil && h.resp.Request.URL != nil {
		h.url = h.resp.Request.URL.String()
	}
	return nil
}

func (h *Http) IsConnected() bool {
	return h.resp != nil && h.resp.Body != nil && !h.resp.Close
}

func (h *Http) GetUrl() string {
	return h.url
}

func (h *Http) GetLastError() error {
	return h.err
}

func (h *Http) Read(buf []byte) (n int, err error) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if h.resp != nil && h.resp.Body != nil {
		n, err = h.resp.Body.Read(buf)
		return
	}
	h.err = http.ErrBodyReadAfterClose
	return 0, http.ErrBodyReadAfterClose
}

func (h *Http) Close() error {
	if h == nil {
		return nil
	}

	h.mutex.Lock()
	defer h.mutex.Unlock()
	var err error
	if h.req != nil && h.req.Body != nil && !h.req.Close {
		err = h.req.Body.Close()
	}
	if h.resp != nil && h.resp.Body != nil && !h.resp.Close {
		err = h.resp.Body.Close()
	}
	h.req = nil
	h.resp = nil
	h.client = nil
	return err
}

func (h *Http) GetHeader(key string) string {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if h.resp != nil && h.resp.Header != nil {
		return h.resp.Header.Get(key)
	}
	return ""
}

func (h *Http) GetHeaders() http.Header {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if h.resp != nil && h.resp.Header != nil {
		return h.resp.Header
	}
	return nil
}

func (h *Http) GetSize() int64 {
	sizeStr := h.GetHeader("Content-Length")
	if sizeStr != "" {
		size, err := strconv.ParseInt(h.resp.Header["Content-Length"][0], 10, 0)
		if err == nil {
			return size
		}
		h.err = err
	} else if sizeStr = h.GetHeader("Content-Range"); sizeStr != "" {
		if cr := strings.Split(sizeStr, "/"); len(cr) > 0 {
			size, err := strconv.ParseInt(cr[len(cr)-1], 10, 0)
			if err == nil {
				return size
			}
			h.err = err
		}
	}
	return -1
}
