package m3u8

/*"errors"
"loader"
"net/url"
"path"
"strings"

"github.com/grafov/m3u8"**/

/*type List struct {
	Url       string
	Content   []string
	Lists     []*List
	Bandwidth uint32
}

func ParseHttp(http *loader.Http) (*List, error) {
	if !http.IsConnected() {
		err := http.Connect()
		if err != nil {
			return nil, err
		}
		defer http.Close()
	}

	p, ltype, err := m3u8.DecodeFrom(http, false)
	if err != nil {
		return nil, err
	}

	retList := new(List)
	murl := http.GetOpts().Url
	retList.Url = murl
	switch ltype {
	case m3u8.MEDIA:
		list := p.(*m3u8.MediaPlaylist)
		if !list.Closed {
			return nil, errors.New("Error m3u8 is live stream, not support")
		}
		for _, s := range list.Segments {
			if s == nil {
				continue
			}
			segmentUrl, err := joinUrl(murl, s.URI)
			if err != nil {
				return nil, err
			}
			retList.Content = append(retList.Content, segmentUrl)
		}
	case m3u8.MASTER:
		list := p.(*m3u8.MasterPlaylist)
		for _, l := range list.Variants {
			if l == nil {
				continue
			}
			listUrl, err := joinUrl(murl, l.URI)
			httpOpt := *http.GetOpts()
			httpOpt.Url = listUrl
			nHttp := loader.NewHttp(&httpOpt)
			nlist, err := ParseHttp(nHttp)
			if err != nil {
				return nil, err
			}
			nlist.Bandwidth = l.Bandwidth
			retList.Lists = append(retList.Lists, nlist)
		}

		for i := 0; i < len(retList.Lists)-1; i++ {
			for j := i + 1; j < len(retList.Lists); j++ {
				if retList.Lists[i].Bandwidth == retList.Lists[j].Bandwidth {
					retList.Lists[i].Bandwidth++
					i--
					break
				}
			}
		}
	}

	return retList, nil
}

func joinUrl(fileUrl, addUrl string) (string, error) {
	if strings.ToLower(addUrl)[:4] == "http" {
		return addUrl, nil
	}

	Url, err := url.Parse(fileUrl)
	if err != nil {
		return "", err
	}
	Url.Path = path.Join(path.Dir(Url.Path), addUrl)
	return Url.String(), nil
}
*/
