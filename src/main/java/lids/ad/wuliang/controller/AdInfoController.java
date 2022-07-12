package lids.ad.wuliang.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lids.ad.wuliang.exception.ResourceConflictException;
import lids.ad.wuliang.exception.ResourceNotFoundException;
import lids.ad.wuliang.model.AdInfo;
import lids.ad.wuliang.service.AppRunner;

import java.util.List;

@RestController
@RequestMapping("/ad")
public class AdInfoController {
    private final AppRunner app;
    @Autowired
    public AdInfoController(AppRunner app){
        this.app = app;
    }
    @GetMapping
    public List<AdInfo> getAdList(){
        return app.getIndexServiceManager().getService().get();
    }
    @GetMapping("/{id}")
    public AdInfo getAdInfo(@PathVariable(value = "id") int adId) throws ResourceNotFoundException{
        if (!app.getIndexServiceManager().getService().contains(adId)){
            throw new ResourceNotFoundException("ad not found for id:"+adId);
        }
        return app.getIndexServiceManager().getService().get(adId);
    }
    @PostMapping
    public AdInfo addAdInfo(@RequestBody AdInfo ad) throws ResourceConflictException {
        if (app.getIndexServiceManager().getService().contains(ad.getAdId())){
            throw new ResourceConflictException("ad already exist for id:"+ad.getAdId());
        }
        return app.getIndexServiceManager().getService().put(ad);
    }
    @PutMapping("{id}")
    public AdInfo updateAdInfo(@PathVariable int id,@RequestBody AdInfo ad) throws ResourceNotFoundException{
        if (!app.getIndexServiceManager().getService().contains(id)){
            throw new ResourceNotFoundException("ad not found for id:"+id);
        }
        return app.getIndexServiceManager().getService().put(ad);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteAdInfo(@PathVariable(value = "id") int adId) throws ResourceNotFoundException{
        if (!app.getIndexServiceManager().getService().contains(adId)){
            throw new ResourceNotFoundException("ad not found for id:"+adId);
        }
        app.getIndexServiceManager().getService().remove(adId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
