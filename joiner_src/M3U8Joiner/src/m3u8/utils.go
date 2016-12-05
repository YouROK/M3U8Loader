package m3u8

import (
	"io/ioutil"
	"loader"
	"path/filepath"
	"strings"
)

func (l *List) readAll(path string) ([]byte, error) {
	dir := strings.ToLower(path)
	if strings.HasPrefix(dir, "http://") || strings.HasPrefix(dir, "https://") {
		hopt := *l.opts.HttpOpts
		hopt.Url = path
		http := loader.NewHttp(&hopt)
		err := http.Connect()
		if err != nil {
			return nil, err
		}
		return ioutil.ReadAll(http)
	} else {
		if filepath.IsAbs(path) {
			return ioutil.ReadFile(path)
		}
		return ioutil.ReadFile(filepath.Join(l.opts.TempDir, path))
	}
}

func getDir(path string) string {
	if strings.HasPrefix(strings.ToLower(path), "http") {
		Url, err := url.Parse(path)
		if err != nil {
			return filepath.Dir(path)
		}
		Url.Path = filepath.Dir(Url.Path)
		return Url.String()
	} else {
		return filepath.Dir(path)
	}
}
