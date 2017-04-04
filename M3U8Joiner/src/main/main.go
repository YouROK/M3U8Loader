package main

import (
	"dwl"
	"dwl/load"
	"dwl/utils"
	"fmt"
	"time"
)

//sets.Url = "https://vootvideo.akamaized.net/enc/fhls/p/1982551/sp/198255100/serveFlavor/entryId/0_g4nuihuo/v/2/pv/1/flavorId/0_11554gdn/name/a.mp4/index.m3u8" //Crypted
//sets.Url = "file:///home/yourok/Dropbox/Projects/GO/M3U8Joiner/src/main/1 - Amedia.m3u"
//sets.Url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
//sets.Url = "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8"
//sets.Url = "http://localhost:8090/files/bipbop/bipbopall.m3u8"
//sets.Url = "http://localhost:8090/files/bipbop/gear1/prog_index.m3u8"
//sets.Url = "http://localhost:8090/files/crypted/crypted.m3u8"
//1080
//sets.Url = "http://185.38.12.47/sec/1489877188/32363634d8ac010fac023a80a61a9994fe8d0870417c35b1/ivs/ec/c6/0c717af81bf8/hls/tracks-1,5/index.m3u8"
//320
//sets.Url = "http://185.38.12.47/sec/1489879297/353538395643fa155f645af09ab272baea980a8c7a354825/ivs/ec/c6/0c717af81bf8/hls/tracks-4,5/index.m3u8"
//Periscope
//sets.Url = "https://prod-video-eu-central-1.periscope.tv/2E5wszX3mrgwHvl3daBru8ueOIiN3sQ87WgcFqT2dqO9BpZjiuO6KC0M8GIfRHL-67PYFUzd3uAS1IQqbTAB-g/replay/eu-central-1/periscope-replay-direct-prod-eu-central-1-public/playlist_1490096320968103978.m3u8"

func main() {
	url := "http://localhost:8090/files/bipbop/gear4/prog_index.m3u8"
	//url := "http://localhost:8090/files/bipbop/bipbopall.m3u8"
	//url := "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8"

	name := "test"

	manager, err := dwl.OpenManager("/home/yourok/tmp/config/")
	if err != nil {
		fmt.Println("Error open manager", err)
	}

	manager.SetSettingsThreads(30)
	manager.SetSettingsDownloadPath("/home/yourok/tmp/")
	manager.SaveSettings()

	if manager.Len() == 0 {
		err = manager.Add(url, name)
		if err != nil {
			fmt.Println("Error add:", err)
			return
		}
	}

	manager.Load(0)
	if manager.GetLoaderInfo(0).Status == load.STATUS_STOPED {
		for {
			if manager.GetLoaderInfo(0).Status != load.STATUS_STOPED {
				break
			}
			time.Sleep(time.Second)
		}
	}

	//go func() {
	//	time.Sleep(time.Second * 10)
	//	manager.Stop(0)
	//}()

	fmt.Println()
	fmt.Println("Url:", manager.GetLoaderInfo(0).Url)
	fmt.Println("Name:", manager.GetLoaderInfo(0).Name)
	fmt.Println("Size:", utils.ByteSize(manager.GetLoaderInfo(0).LoadedBytes))
	go func() {
		for {

			info := manager.GetLoaderInfo(0)
			st := info.Status
			fmt.Println("Status:", st)
			fmt.Println("Threads:", info.Threads)
			fmt.Println("Speed:", utils.ByteSize(info.Speed))
			fmt.Println("Loaded:", info.Completed, "/", info.LoadingCount, info.Completed*100/info.LoadingCount, "% ", utils.ByteSize(info.LoadedBytes))
			fmt.Println()
			if st == load.STATUS_COMPLETE {
				fmt.Println("Complete")
			}
			if st == load.STATUS_ERROR {
				fmt.Println("Error load:", info.Error)
			}
			if st == load.STATUS_STOPED {
				fmt.Println("Load stoped")
			}
			time.Sleep(time.Second)
			fmt.Println()
		}
	}() /**/
	manager.WaitLoader(0)
	fmt.Println("FINISH")
	time.Sleep(time.Second * 2)
}
