package com.sbpinvertor.modbus;

import com.sbpinvertor.modbus.exception.ModbusNumberException;
import com.sbpinvertor.modbus.exception.ModbusTransportException;
import com.sbpinvertor.modbus.msg.ModbusRequestFactory;
import com.sbpinvertor.modbus.msg.base.ModbusMessage;
import com.sbpinvertor.modbus.msg.response.ReadCoilsResponse;
import com.sbpinvertor.modbus.msg.response.ReadDiscreteInputsResponse;
import com.sbpinvertor.modbus.msg.response.ReadHoldingRegistersResponse;
import com.sbpinvertor.modbus.msg.response.ReadInputRegistersResponse;
import com.sbpinvertor.modbus.net.ModbusConnection;
import com.sbpinvertor.modbus.net.ModbusTransport;
import com.sbpinvertor.modbus.serial.SerialPortException;

import java.io.IOException;

/**
 * Copyright (c) 2015-2016 JSC "Zavod "Invertor"
 * [http://www.sbp-invertor.ru]
 * <p/>
 * This file is part of JLibModbus.
 * <p/>
 * JLibModbus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Authors: Vladislav Y. Kochedykov, software engineer.
 * email: vladislav.kochedykov@gmail.com
 */
abstract public class ModbusMaster {

    final private ModbusRequestFactory requestFactory = ModbusRequestFactory.getInstance();

    protected ModbusMaster() {

    }

    abstract protected ModbusTransport getTransport();

    abstract protected ModbusConnection getConnection();

    protected void sendRequest(ModbusMessage msg) throws ModbusTransportException, IOException {
        getTransport().send(msg);
    }

    protected ModbusMessage readResponse() throws ModbusTransportException, ModbusNumberException, IOException {
        return getTransport().readResponse();
    }

    private ModbusMessage processRequest(ModbusMessage request) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        sendRequest(request);
        ModbusMessage msg = readResponse();
        if (request.getProtocolId() != msg.getProtocolId())
            throw new ModbusTransportException("Collision: does not matches the transaction id");
        if (request.getTransactionId() != msg.getTransactionId())
            throw new ModbusTransportException("Collision: does not matches the transaction id");
        if (request.getServerAddress() != msg.getServerAddress())
            throw new ModbusTransportException("Collision: does not matches the slave address");
        if (request.getFunction() != msg.getFunction())
            throw new ModbusTransportException("Collision: does not matches the function code");
        return msg;
    }

    /**
     * ModbusMaster will block for only this amount of time.
     * If the timeout expires, a ModbusTransportException is raised, though the ModbusMaster is still valid.
     *
     * @param timeout the specified timeout, in milliseconds.
     */
    public void setResponseTimeout(int timeout) {
        getConnection().setReadTimeout(timeout);
    }

    final public int[] readHoldingRegisters(int serverAddress, int startAddress, int quantity) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        ModbusMessage request = requestFactory.createReadHoldingRegisters(serverAddress, startAddress, quantity);
        ReadHoldingRegistersResponse response = (ReadHoldingRegistersResponse) processRequest(request);
        return response.getRegisters();
    }

    final public int[] readInputRegisters(int serverAddress, int startAddress, int quantity) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        ModbusMessage request = requestFactory.createReadInputRegisters(serverAddress, startAddress, quantity);
        ReadHoldingRegistersResponse response = (ReadInputRegistersResponse) processRequest(request);
        return response.getRegisters();
    }

    final public boolean[] readCoils(int serverAddress, int startAddress, int quantity) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        ModbusMessage request = requestFactory.createReadCoils(serverAddress, startAddress, quantity);
        ReadCoilsResponse response = (ReadCoilsResponse) processRequest(request);
        return response.getCoils();
    }

    final public boolean[] readDiscreteInputs(int serverAddress, int startAddress, int quantity) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        ModbusMessage request = requestFactory.createReadDiscreteInputs(serverAddress, startAddress, quantity);
        ReadDiscreteInputsResponse response = (ReadDiscreteInputsResponse) processRequest(request);
        return response.getCoils();
    }

    final public void writeSingleCoil(int serverAddress, int startAddress, boolean flag) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        processRequest(requestFactory.createWriteSingleCoil(serverAddress, startAddress, flag));
    }

    final public void writeSingleRegister(int serverAddress, int startAddress, int register) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        processRequest(requestFactory.createWriteSingleRegister(serverAddress, startAddress, register));
    }

    final public void writeMultipleCoils(int serverAddress, int startAddress, boolean[] coils) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        processRequest(requestFactory.createWriteMultipleCoils(serverAddress, startAddress, coils));
    }

    final public void writeMultipleRegisters(int serverAddress, int startAddress, int[] registers) throws SerialPortException,
            ModbusTransportException, ModbusNumberException, IOException {
        processRequest(requestFactory.createWriteMultipleRegisters(serverAddress, startAddress, registers));
    }
}
