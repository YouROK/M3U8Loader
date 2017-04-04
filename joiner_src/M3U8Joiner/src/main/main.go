package main

import (
	"fmt"
	"log"
	"m3u8"
)

func main() {
	opt := m3u8.NewOptions()
	opt.TempDir = "/home/yourok/tmp/"
	//	opt.Url = "file:///home/yourok/test.m3u"
	//	opt.Url = "http://api.new.livestream.com/accounts/22931184/events/6846118/videos/146115126.m3u8"
	//	opt.Url = "http://atv-vod.ercdn.net/eskiya_dunyaya_hukumdar_olmaz/050/eskiya_dunyaya_hukumdar_olmaz_050.smil/chunklist_b1200000_t64MTIwMGticHM=.m3u8?st=UMUgMJhxlvjQEdm1fxfT_Q&e=1484251384&SessionID=1qhyziauy3dwxx4yxfydiu2m&StreamGroup=eskiya-dunyaya-hukumdar-olmaz&Site=atv&DeviceGroup=web"
	//	opt.Url = "file:///home/yourok/Projects/Android/M3U8Loader/joiner_src/M3U8Joiner/src/main/a.m3u8"
	//	opt.Url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
	//	opt.Url = "https://vootvideo.akamaized.net/enc/fhls/p/1982551/sp/198255100/serveFlavor/entryId/0_g4nuihuo/v/2/pv/1/flavorId/0_11554gdn/name/a.mp4/index.m3u8"
	opt.Url = "http://localhost:8090/files/crypted/crypted.m3u8"

	opt.Name = "crypted1"
	opt.OutFileDir = opt.TempDir
	opt.Threads = 30
	opt.Timeout = 0

	m := m3u8.NewM3U8(opt)
	go func() {
		var err error
		if m.GetList() == nil || m.GetList().Url != opt.Url {
			err = m.LoadList()
		}
		if err == nil {
			for i := 1; i < m.GetList().ListsSize(); i++ {
				m.GetList().GetList(i).IsLoad = false
			}
			/*for i := 1; i < m.GetList().ItemsSize(); i++ {
				m.GetList().GetItem(i).IsLoad = false
			}*/
			for true {
				fmt.Println("Load")
				err = m.Load()
				if err == nil {
					break
				}
				if err != nil {
					fmt.Println(err)
				}
			}
			if err == nil {
				fmt.Println("Join")
				err = m.Join()
				if err == nil {
					fmt.Println("Remove temp")
					//					err = m.RemoveTemp()
					if err == nil {
						m.Finish()
					}
				}
			}
		}
		if err != nil {
			fmt.Println("Error work", err)
		}
	}()
	//	stoped := 0
	log.Println("receive state")
	sg := m3u8.Stage_LoadingList
	for true {
		st := m3u8.GetState(m)
		if st == nil {
			continue
		}
		sg = st.Stage
		log.Println(st, m.Speed())
		if sg == m3u8.Stage_Finished {
			break
		}
	}
	fmt.Println("Exit")
}
