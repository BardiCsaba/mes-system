package pt.feup.industrial.mes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.feup.industrial.mes.model.Machine;
import pt.feup.industrial.mes.model.Tool;
import pt.feup.industrial.mes.repository.MachineRepository;
import pt.feup.industrial.mes.repository.ToolRepository;

import java.util.List;

@RestController
@RequestMapping("/api/mes")
public class MesMasterDataController {

    private static final Logger log = LoggerFactory.getLogger(MesMasterDataController.class);

    private final MachineRepository machineRepository;
    private final ToolRepository toolRepository;

    @Autowired
    public MesMasterDataController(MachineRepository machineRepository, ToolRepository toolRepository) {
        this.machineRepository = machineRepository;
        this.toolRepository = toolRepository;
    }

    @GetMapping("/machines")
    public List<Machine> getAllMachines() {
        log.debug("Request received for all machines");
        return machineRepository.findAll();
    }

    @GetMapping("/machines/by-name/{machineName}")
    public ResponseEntity<Machine> getMachineByName(@PathVariable String machineName) {
        log.debug("Request received for machine name: {}", machineName);
        return machineRepository.findByMachineName(machineName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tools")
    public List<Tool> getAllTools() {
        log.debug("Request received for all tools");
        return toolRepository.findAll();
    }

    @GetMapping("/tools/by-name/{toolName}")
    public ResponseEntity<Tool> getToolByName(@PathVariable String toolName) {
        log.debug("Request received for tool name: {}", toolName);
        return toolRepository.findByToolName(toolName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}