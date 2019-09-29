package com.wh.extendexoplayer.player;

import com.wh.extendexoplayer.widget.RendererView;

abstract class AbstractBuilder<T> {

    protected T t;

    protected RendererView mRendererView;

    protected boolean isFitXY = true;

    public T rendererView(RendererView rendererView){
        this.mRendererView = rendererView;
        return t;
    }

    public T fitXY(boolean fitXY){
        this.isFitXY = fitXY;
        return t;
    }

    void checkRendererViewNotNULL(){
        if(mRendererView == null){
            throw new RuntimeException("mRendererView Can not be null");
        }
    }
}
