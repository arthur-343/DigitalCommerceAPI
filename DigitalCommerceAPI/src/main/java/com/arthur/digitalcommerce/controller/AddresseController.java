package com.arthur.digitalcommerce.controller;

import com.arthur.digitalcommerce.payload.AddressDTO;
import com.arthur.digitalcommerce.service.AddressesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping("/api/")
public class AddresseController {

    @Autowired
    AddressesService addressesService;

    @GetMapping("admin/all/addresses")
    public ResponseEntity<List<AddressDTO>> getAllAddress(){
        List<AddressDTO> addressDTOs = addressesService.getAllAddress();
        return new  ResponseEntity<List<AddressDTO>>(addressDTOs, HttpStatus.OK);
    }

    @PostMapping("public/create/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO address){
        AddressDTO addressDTO = addressesService.createAddress(address);
        return new  ResponseEntity<AddressDTO>(addressDTO, HttpStatus.CREATED);
    }

    @GetMapping("public/addresses/user")
    public ResponseEntity<List<AddressDTO>> getAllAddressesByUser(){
        List<AddressDTO> addressDTOs = addressesService.getAllAddressesByUser();
        return new  ResponseEntity<List<AddressDTO>>(addressDTOs, HttpStatus.OK);
    }

    @GetMapping("public/addresses/id/{addressId}")
    public ResponseEntity<AddressDTO> getByIdAddress( @PathVariable  Long addressId){
        AddressDTO addressDTO = addressesService.getByIdAddress(addressId);
        return new  ResponseEntity<AddressDTO>(addressDTO, HttpStatus.OK);
    }

    @PutMapping("public/addresses/update/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDTO addressDTO) {

        AddressDTO updatedAddress = addressesService.updateAddress(addressId, addressDTO);
        return new ResponseEntity<>(updatedAddress, HttpStatus.OK);
    }

    @DeleteMapping("public/addresses/delete/{addressId}")
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        String resultMessage = addressesService.deletingAddress(addressId);
        return new ResponseEntity<>(resultMessage, HttpStatus.OK);
    }
}
