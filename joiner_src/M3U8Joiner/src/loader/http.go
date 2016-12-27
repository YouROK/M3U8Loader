package loader

import (
	"errors"
	"net"
	"net/http"
	"sync"
	"time"
)

type Http struct {
	client *http.Client
	req    *http.Request
	resp   *http.Response
	opt    *HttpOpts

	mutex sync.Mutex
	err   error
}

func NewHttp(opt *HttpOpts) *Http {
	con := &Http{}
	con.opt = opt
	return con
}

func (h *Http) GetOpts() *HttpOpts {
	return h.opt
}

func (h *Http) Connect() error {
	h.mutex.Lock()
	h.client = &http.Client{}
	h.req, h.err = http.NewRequest("GET", h.opt.Url, nil)
	h.mutex.Unlock()
	if h.err != nil {
		defer h.Close()
		return h.err
	}

	for v, k := range h.opt.header {
		h.req.Header.Add(v, k)
	}

	h.mutex.Lock()
	h.client.Timeout = time.Millisecond * time.Duration(h.opt.Timeout)
	var netTransport = &http.Transport{
		Dial: (&net.Dialer{
			Timeout: h.client.Timeout,
		}).Dial,
		TLSHandshakeTimeout:   h.client.Timeout,
		ResponseHeaderTimeout: h.client.Timeout,
		ExpectContinueTimeout: h.client.Timeout,
		IdleConnTimeout:       h.client.Timeout,
	}
	h.client.Transport = netTransport
	h.resp, h.err = h.client.Do(h.req)
	if h.resp != nil && h.resp.StatusCode != http.StatusOK {
		h.err = errors.New(h.resp.Status)
	}
	h.mutex.Unlock()

	if h.err != nil {
		h.Close()
		return h.err
	}
	if h.resp != nil && h.resp.Request != nil && h.resp.Request.URL != nil {
		h.opt.Url = h.resp.Request.URL.String()
	}
	return nil
}

func (h *Http) IsConnected() bool {
	return h.resp != nil && h.resp.Body != nil && !h.resp.Close
}

func (h *Http) GetLastError() error {
	return h.err
}

func (h *Http) Read(buf []byte) (n int, err error) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if h.resp != nil && h.resp.Body != nil {
		return h.resp.Body.Read(buf)
	}
	h.err = http.ErrBodyReadAfterClose
	return 0, http.ErrBodyReadAfterClose
}

func (h *Http) Close() {
	if h == nil {
		return
	}

	h.mutex.Lock()
	defer h.mutex.Unlock()
	if h.req != nil && h.req.Body != nil && !h.req.Close {
		h.req.Body.Close()
	}
	if h.resp != nil && h.resp.Body != nil && !h.resp.Close {
		h.resp.Body.Close()
	}
	h.req = nil
	h.resp = nil
	h.client = nil
}
