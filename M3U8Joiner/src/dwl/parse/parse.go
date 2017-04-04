package parse

import ()

/*
type ParseList struct {
	sets  *settings.Settings
	lists []*list.List
}

func NewParse(sets *settings.Settings) *ParseList {
	p := new(ParseList)
	p.sets = sets
	return p
}

func (p *ParseList) GetSettings() *settings.Settings {
	return p.sets
}

func (p *ParseList) GetLists() []*list.List {
	return p.lists
}

func (p *ParseList) Parse(Url string) ([]*list.List, error) {
	if Url == "" {
		Url = p.sets.Url
	}

	sets := p.sets.CloneUrl(Url)
	buf, err := utils.ReadBuf(sets)
	if err != nil {
		return nil, err
	}

	plist, _, err := m3u8.DecodeFrom(bytes.NewBuffer(buf), false)
	if err != nil {
		return nil, err
	}

	lists, err := p.parseList(plist, Url)
	if err != nil {
		return nil, err
	}
	p.lists = append(p.lists, lists...)
	return lists, nil
}

func (p *ParseList) parseList(parse m3u8.Playlist, urlList string) ([]*list.List, error) {
	retList := make([]*list.List, 0)

	baseUrl, err := utils.DirUrl(urlList)
	if err != nil {
		return nil, err
	}

	switch mlist := parse.(type) {
	case *m3u8.MediaPlaylist:
		{
			//Parse items of parse
			rlist := new(list.List)
			rlist.Url = urlList
			rlist.Name = p.sets.Name
			rlist.IsLoad = true

			if mlist.Key != nil {
				keyUrl, err := utils.JoinUrl(baseUrl, mlist.Key.URI)
				if err != nil {
					return nil, err
				}
				key, err := p.loadKey(keyUrl, mlist.Key.IV)
				if err != nil {
					return nil, err
				}
				rlist.EncKey = key
			}

			for _, i := range mlist.Segments {
				if i == nil || i.URI == "" {
					continue
				}

				iurl, err := utils.JoinUrl(baseUrl, i.URI)

				if err != nil {
					return nil, err
				}

				//if item is parse
				if strings.HasSuffix(strings.ToLower(i.URI), "m3u8") || strings.HasSuffix(strings.ToLower(i.URI), "m3u") {

					ll, err := p.Parse(iurl)
					if err != nil {
						return nil, err
					}
					for k, itm := range ll {
						itm.Bandwidth = k
						itm.Name = i.Title
						if itm.Name == "" {
							itm.Name = p.sets.Name + "." + fmt.Sprint(itm.Bandwidth)
						}
					}
					if len(ll) > 0 {
						retList = append(retList, ll...)
					}
				} else {
					// if items in media
					itm := list.Item{}
					itm.Url = iurl
					itm.IsLoad = true
					itm.Index = len(rlist.Items)
					rlist.Items = append(rlist.Items, &itm)
				}
			}
			if len(rlist.Items) > 0 {
				retList = append(retList, rlist)
			}
		}
	case *m3u8.MasterPlaylist:
		{
			//Parse lists of parse
			for _, i := range mlist.Variants {
				if i == nil || i.URI == "" {
					continue
				}
				iurl, err := utils.JoinUrl(baseUrl, i.URI)
				if err != nil {
					return nil, err
				}
				ll, err := p.Parse(iurl)
				if err != nil {
					return nil, err
				}
				for k, itm := range ll {
					itm.Bandwidth = int(i.Bandwidth)
					if itm.Bandwidth == 0 {
						itm.Bandwidth = k
					}
					itm.Name = i.Name
					if itm.Name == "" {
						itm.Name = p.sets.Name + "." + fmt.Sprint(itm.Bandwidth)
					}
				}
				if len(ll) > 0 {
					retList = append(retList, ll...)
				}
			}
		}
	}
	return retList, nil
}
*/
