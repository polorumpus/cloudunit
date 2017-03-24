package fr.treeptik.cloudunit.orchestrator.resource;

import org.springframework.hateoas.ResourceSupport;

import fr.treeptik.cloudunit.orchestrator.core.Volume;

public class VolumeResource extends ResourceSupport {
    private String name;

    public VolumeResource() {}
    
    public VolumeResource(String name) {
        this.name = name;
    }
    
    public VolumeResource(Volume volume) {
        this.name = volume.getName();
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
