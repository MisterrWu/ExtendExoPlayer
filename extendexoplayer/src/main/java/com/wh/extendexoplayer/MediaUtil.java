package com.wh.extendexoplayer;

public class MediaUtil {

    private long prevFrmNo = -1;
    private int prevIFrameIndex = -1;

    /**
     * 解决丢帧问题，避免出现花屏。
     * 1.第一个判断，保证首次解码播放是从关键帧开始播放。
     * 2.第二个判断，frmNo 是递增的情况，只要丢了一帧（包括I帧，p帧）只有到下一个关键帧才是有效帧。
     * 3.第三个判断，frmNo 不递增的情况，无法判断丢p帧，只要丢了一帧（只包括I帧）只有到下一个关键帧才是有效帧。。
     */
    public boolean isVideoInfoValid(boolean isIFrame, long frmNo, int iFrameIndex) {
        boolean isValid = false;
        if (isIFrame) {
            prevIFrameIndex = iFrameIndex;
            prevFrmNo = frmNo;
            isValid = true;
        } else if (prevFrmNo != -1 && frmNo - prevFrmNo == 1) {
            prevFrmNo = frmNo;
            isValid = true;
        }else if(frmNo == prevFrmNo && prevIFrameIndex == iFrameIndex){
            isValid = true;
        }
        return isValid;
    }
}
