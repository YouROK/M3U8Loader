package main

import (
	"dwl"
	"dwl/load"
	"dwl/utils"
	"fmt"
	"time"
)

func main() {

	//url := "http://localhost:8090/files/bipbop/gear4/prog_index.m3u8"
	//url := "http://localhost:8090/files/bipbop/bipbopall.m3u8"
	//url := "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8"
	//url := "http://localhost:8090/files/crypted/crypted.m3u8"
	//url := "http://185.38.12.34/sec/1496458923/323131357cecbe77d0ac3da12733b01d3c9e4f8d9a5ccbd3/ivs/14/b2/cd247efb5db6.mp4/hls/tracks-3,4/index.m3u8"
	url := "http://185.38.12.57/sec/1503638410/3837333428f3075f0647ee52b9dc7dbb353bbd50f480c462/ivs/9d/6e/6e1dedb079d7.mp4/hls/tracks-1,7/index.m3u8"
	name := "test"

	manager, err := dwl.OpenManager("/home/yourok/tmp/video/config/")
	if err != nil {
		fmt.Println("Error open manager", err)
	}

	manager.SetSettingsThreads(30)
	manager.SetSettingsDownloadPath("/home/yourok/tmp/video/")
	//manager.SetSettingsDynamicSize(true)
	//manager.SetSettingsLoadItemsSize(true)
	manager.SaveSettings()

	if manager.Len() == 0 {
		err = manager.Add(url, name, "", "") //"Token=1492582676; Service=proxsee; Digest=ToHLzuPSFh9788bM-4tSQsk6_AlgYDODLNv_UldQ7mE", "")
		if err != nil {
			fmt.Println("Error add:", err)
			return
		}
	} else {
		manager.Clean(0)
	}

	fmt.Println(utils.ByteSize(manager.GetLoaderInfo(0).Size))

	//manager.SetSubtitles(0,"http://185.38.12.39/static/srt/uploads/srt_master_file/219769/d87add9d880b4ab8.srt")
	//err = manager.LoadSubtitles(0)
	//if err != nil {
	//	fmt.Println("Error load subtitles:", err)
	//}

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
	//	time.Sleep(time.Second * 5)
	//	manager.Stop(0)
	//	time.Sleep(time.Second * 1)
	//	manager.Load(0)
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
			fmt.Println("Duration:", utils.TimeSize(info.LoadedDuration), "/", utils.TimeSize(info.Duration))
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
	for !manager.WaitLoader(0, 2000) {
		time.Sleep(time.Second)
	}
	fmt.Println("FINISH")
	time.Sleep(time.Second * 2)
}
