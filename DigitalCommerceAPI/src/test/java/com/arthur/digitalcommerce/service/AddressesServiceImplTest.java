package com.arthur.digitalcommerce.service;

import com.arthur.digitalcommerce.exceptions.APIException;
import com.arthur.digitalcommerce.exceptions.ResourceNotFoundException;
import com.arthur.digitalcommerce.model.Address;
import com.arthur.digitalcommerce.model.User;
import com.arthur.digitalcommerce.payload.AddressDTO;
import com.arthur.digitalcommerce.repository.AddressRepository;
import com.arthur.digitalcommerce.repository.UserRepository;
import com.arthur.digitalcommerce.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressesServiceImplTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthUtil authUtil;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AddressesServiceImpl addressesService;

    private User user;
    private Address address;
    private AddressDTO addressDTO;
    private Long addressId = 1L;
    private String userEmail = "test@user.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail(userEmail);
        user.setAddresses(new ArrayList<>());

        address = new Address();
        address.setAddressId(addressId);
        address.setStreet("Test Street");
        address.setUser(user);

        addressDTO = new AddressDTO();
        addressDTO.setAddressId(addressId);
        addressDTO.setStreet("Test Street");
    }

    @Test
    void createAddress_shouldReturnSavedAddressDTO_whenUserIsLoggedIn() {
        when(authUtil.loggedInUser()).thenReturn(user);
        when(modelMapper.map(any(AddressDTO.class), eq(Address.class))).thenReturn(address);
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);

        AddressDTO result = addressesService.createAddress(addressDTO);

        assertNotNull(result);
        assertEquals(addressDTO.getStreet(), result.getStreet());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void getAllAddress_shouldReturnListOfAddressDTOs() {
        List<Address> addresses = Collections.singletonList(address);
        when(addressRepository.findAll()).thenReturn(addresses);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);

        List<AddressDTO> result = addressesService.getAllAddress();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getByIdAddress_shouldReturnAddressDTO_whenAddressExists() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);

        AddressDTO result = addressesService.getByIdAddress(addressId);

        assertNotNull(result);
        assertEquals(addressId, result.getAddressId());
    }

    @Test
    void getByIdAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> addressesService.getByIdAddress(addressId));
    }

    @Test
    void getAllAddressesByUser_shouldReturnUserAddresses_whenUserIsLoggedIn() {
        when(authUtil.loggedInEmail()).thenReturn(userEmail);
        when(addressRepository.findByUserEmail(userEmail)).thenReturn(Collections.singletonList(address));
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);

        List<AddressDTO> result = addressesService.getAllAddressesByUser();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddressDTO_whenAddressExists() {
        AddressDTO updateDTO = new AddressDTO();
        updateDTO.setCity("New City");

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenReturn(address);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(Address.class), eq(AddressDTO.class))).thenReturn(addressDTO);

        addressesService.updateAddress(addressId, updateDTO);

        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository, times(1)).save(addressCaptor.capture());
        Address capturedAddress = addressCaptor.getValue();

        assertEquals("New City", capturedAddress.getCity());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressNotFound() {
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> addressesService.updateAddress(addressId, addressDTO));
    }

    @Test
    void deletingAddress_shouldSucceed_whenAddressExistsAndBelongsToUser() {
        when(authUtil.loggedInEmail()).thenReturn(userEmail);
        when(addressRepository.findByIdAndUserEmail(addressId, userEmail)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).deleteById(addressId);

        String result = addressesService.deletingAddress(addressId);

        assertEquals("Address deleted successfully.", result);
        verify(addressRepository, times(1)).deleteById(addressId);
    }

    @Test
    void deletingAddress_shouldThrowException_whenAddressNotFoundForUser() {
        when(authUtil.loggedInEmail()).thenReturn(userEmail);
        when(addressRepository.findByIdAndUserEmail(addressId, userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressesService.deletingAddress(addressId));
        verify(addressRepository, never()).deleteById(anyLong());
    }
}