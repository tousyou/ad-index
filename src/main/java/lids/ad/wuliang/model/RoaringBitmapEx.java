package lids.ad.wuliang.model;

import org.roaringbitmap.RoaringBitmap;

import java.util.concurrent.locks.ReentrantReadWriteLock;

//RoaringBitmap参见：https://github.com/RoaringBitmap/RoaringBitmap
//RoaringBitmapEx是一个线程安全的扩展
public class RoaringBitmapEx extends RoaringBitmap {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public void clear(){
        readWriteLock.writeLock().lock();
        super.clear();
        readWriteLock.writeLock().unlock();
    }
    public boolean contains(int x){
        readWriteLock.readLock().lock();
        boolean isContains = super.contains(x);
        readWriteLock.readLock().unlock();
        return isContains;
    }
    public void add(int x){
        readWriteLock.writeLock().lock();
        super.add(x);
        readWriteLock.writeLock().unlock();
    }
    public void remove(int x){
        readWriteLock.writeLock().lock();
        super.remove(x);
        readWriteLock.writeLock().unlock();
    }
    public void threadSafeOr(RoaringBitmap retRb){
        readWriteLock.writeLock().lock();
        retRb.or(this);
        readWriteLock.writeLock().unlock();
    }
}
