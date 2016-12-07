package loader

import (
	"runtime"
)

type HttpOpts struct {
	Url     string
	Timeout int
	header  map[string]string
}

func NewHttpOpts(url string) *HttpOpts {
	o := new(HttpOpts)
	o.header = make(map[string]string)
	o.Timeout = 5000
	o.SetHeader("Accept", "*/*")
	o.SetHeader("UserAgent", "DWL/1.0.0 ("+runtime.GOOS+")")
	o.Url = url
	return o
}

func (o *HttpOpts) SetHeader(key, val string) {
	if o.header == nil {
		o.header = make(map[string]string)
	}
	o.header[key] = val
}

func (o *HttpOpts) SetUrl(url string) {
	o.Url = url
}

func (o *HttpOpts) SetTimeout(timeout int) {
	o.Timeout = timeout
}
