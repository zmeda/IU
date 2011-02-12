package iu.server.explore.game;

import iu.android.network.explore.Protocol;
import iu.android.util.Converter;
import iu.server.Log;

public class ProtocolParser
{
	public static Command parse (final byte[] arr, final Game game, final Player player)
	{
		Command command = null;

		//
		// check for message
		//

		switch (arr[0])
		{
			case Protocol.LOCATION_UPDATE:
				{
					// { command [byte] | player id [int] | latitude [float] | longitude [float] }

					//int playerId = Converter.bytesToInt (arr, 1);
					float lat = Converter.bytesToFloat (arr, 5);
					float lon = Converter.bytesToFloat (arr, 9);

					Location loc = new Location (lat, lon);

					command = CommandMovePlayer.init (player, loc);
					break;
				}
			case Protocol.GET_FLAG_UNITS:
				{
					// { command [byte] | flagId [int] }
					Integer flagId = new Integer (Converter.bytesToInt (arr, 1));
					Flag flag = game.getFlag (flagId);

					command = CommandGetFlagUnits.init (player, flag);
					break;
				}
			case Protocol.SET_FLAG_UNITS:

				{
					// { command [byte] | flagId [int] | hover [short] | tank [short] | artillery [short] }
					Integer flagId = new Integer (Converter.bytesToInt (arr, 1));
					Flag flag = game.getFlag (flagId);
					short hoverNum = Converter.bytesToShort (arr, 5);
					short tankNum = Converter.bytesToShort (arr, 7);
					short artNum = Converter.bytesToShort (arr, 9);

					command = CommandSetFlagUnits.init (flag, hoverNum, tankNum, artNum);
					break;
				}
			case Protocol.GET_PLAYER_PROPERTIES:
				{
					// { command [byte] | playerId[int] }
					Integer playerId = new Integer (Converter.bytesToInt (arr, 1));
					Player p = game.getPlayer (playerId);

					command = CommandGetPlayerProperties.init (game, p);
					break;
				}
			case Protocol.SET_PLAYER_PROPERTIES:
				{
					// { command [byte] | playerId [int] | hover[short] , tank[short] , artillery[short] }

					Integer playerId = new Integer (Converter.bytesToInt (arr, 1));
					Player p = game.getPlayer (playerId);
					
					System.out.println("Setting player ID=" + playerId + "  -  " + p);

					short hoverNum = Converter.bytesToShort (arr, 5);
					short tankNum = Converter.bytesToShort (arr, 7);
					short artNum = Converter.bytesToShort (arr, 9);
					
					System.out.println("Units: H=" + hoverNum + " T=" + tankNum + " A=" + artNum);

					command = CommandSetPlayerProperties.init (p, hoverNum, tankNum, artNum);
					break;
				}
			case Protocol.LOGOUT:
				{
					command = CommandLogOutPlayer.init (game, player);
					break;
				}
				
			case Protocol.GET_BATTLE:
				{
					// { command [byte] | battleId[int] }
					int battleId = Converter.bytesToInt (arr, 1);
					FlagBattle battle = (FlagBattle) game.getBattle (battleId);
					// TODO When we have more Battle types we can check for instanceof here ...
					
					command = CommandGetFlagBattle.init (battle, player);
					break;
				}
					
			case Protocol.FLUSH_EVENTS:
				{
					break;
				}
			case Protocol.BUFFER_EVENTS:
				{
					break;
				}

			case Protocol.ATTACK_FLAG:
			{
				// { command [byte] | flagId [int] | hover [short] | tank [short] | artillery[short] }
				int flagId = Converter.bytesToInt (arr, 1);
				short hover = Converter.bytesToShort (arr, 5);
				short tank = Converter.bytesToShort (arr, 7);
				short artilllery = Converter.bytesToShort (arr, 9);

				Flag flag = game.getFlag (flagId);
				FlagBattle battle = new FlagBattle (player, flag);
				UnitConfiguration units = new UnitConfiguration (hover, tank, artilllery);
				battle.attackerUnits = units;
				command = CommandAttack.init (battle);
				break;
			}	
			case Protocol.DEFFEND_FLAG:
			{
				// { command [byte] | battleId [int] | hover [short] | tank [short] | artillery [short] }
				int battleId = Converter.bytesToInt (arr, 1);
				short hover = Converter.bytesToShort (arr, 5);
				short tank = Converter.bytesToShort (arr, 7);
				short artilllery = Converter.bytesToShort (arr, 9);
				
				FlagBattle battle = (FlagBattle) game.getBattle (battleId);
				UnitConfiguration units = new UnitConfiguration (hover, tank, artilllery);
				battle.defenderUnits = units;
				command = CommandDefend.init (battle);
				break;
			}
			case Protocol.FINISH_BATTLE:
			{
				// { command [byte] | battleId [int] | winner[int] }
				int battleId = Converter.bytesToInt (arr, 1);
				int winnerId = Converter.bytesToInt (arr, 5);
				
				Battle battle = game.getBattle (battleId);
				Player winner = game.getPlayer (winnerId);
				
				command = CommandFinishBattle.init (battle, winner);
				break;
			}
			default:
				{
					throw new UnsupportedOperationException ("Unknown Protocol Command:" + arr[0]);
				}
		}

		if (command == null) {
			Log.Lucas.e ("Protocol", "Not implemented for command " + arr[0]);
		}
		
		return command;
	}
	//
	// parse messages
	//

}
