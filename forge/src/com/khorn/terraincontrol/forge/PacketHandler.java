package com.khorn.terraincontrol.forge;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.src.INetworkManager;
import net.minecraft.src.Packet250CustomPayload;
import net.minecraft.src.WorldClient;

import com.khorn.terraincontrol.configuration.TCDefaultValues;
import com.khorn.terraincontrol.configuration.WorldConfig;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler
{
    TCPlugin plugin;

    public PacketHandler(TCPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload receivedPacket, Player player)
    {
        // This method receives the TerrainControl packet with the custom biome colors and weather.
        
        if (!receivedPacket.channel.equals(TCDefaultValues.ChannelName.stringValue()))
        {
            // Make sure that the right channel is being received
            return;
        }
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.CLIENT)
        {
            // Make sure that we are on the client (the server only sends packets to the client)
            return;
        }

        // We're on the client, receive the packet
        ByteArrayInputStream inputStream = new ByteArrayInputStream(receivedPacket.data);
        DataInputStream stream = new DataInputStream(inputStream);
        try
        {
            int serverProtocolVersion = stream.readInt();
            int clientProtocolVersion = TCDefaultValues.ProtocolVersion.intValue();
            if (serverProtocolVersion == clientProtocolVersion)
            {
                // Server sent config
            	
            	// Restore old biomes
            	SingleWorld.restoreBiomes();
            
            	if(receivedPacket.length > 4) {
            		// If the packet wasn't empty, add the new biomes
            		WorldClient worldMC = FMLClientHandler.instance().getClient().theWorld;
                    
                    SingleWorld worldTC = new SingleWorld("external");
                    WorldConfig config = new WorldConfig(stream, worldTC);

                    worldTC.InitM(worldMC, config);
            	}
                

                System.out.println("TerrainControl: config received from server");
            } else
            {
                // Server or client is outdated
                System.out.println("TerrainControl: server has different protocol version! " + "Client: " + TCDefaultValues.ProtocolVersion.intValue() + " Server: " + serverProtocolVersion);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
