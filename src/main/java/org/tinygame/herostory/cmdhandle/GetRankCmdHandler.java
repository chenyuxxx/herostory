package org.tinygame.herostory.cmdhandle;

import io.netty.channel.ChannelHandlerContext;
import org.tinygame.herostory.msg.GameMsgProtocol;
import org.tinygame.herostory.rank.RankItem;
import org.tinygame.herostory.rank.RankService;

import java.util.Collections;

/**
 * 获得排行榜指令处理器
 */
public class GetRankCmdHandler implements ICmdHandler< GameMsgProtocol.GetRankCmd> {

    @Override
    public void handle(ChannelHandlerContext ctx, GameMsgProtocol.GetRankCmd cmd) {
        if (null == ctx
                || null == cmd) {
            return;
        }

        RankService.getInstance().getRank((rankItemsList) -> {
            if (null == rankItemsList){
                rankItemsList = Collections.emptyList();
            }

            GameMsgProtocol.GetRankResult.Builder resultBuilder = GameMsgProtocol.GetRankResult.newBuilder();

            for (RankItem rankItem : rankItemsList){
                GameMsgProtocol.GetRankResult.RankItem.Builder
                        rankItemBuilder = GameMsgProtocol.GetRankResult.RankItem.newBuilder();

                rankItemBuilder.setRankId(rankItem.getRankId());
                rankItemBuilder.setUserId(rankItem.getUserId());
                rankItemBuilder.setUserName(rankItem.getUserName());
                rankItemBuilder.setHeroAvatar(rankItem.getHeroAvatar());
                rankItemBuilder.setWin(rankItem.getWin());

                resultBuilder.addRankItem(rankItemBuilder);
            }

            GameMsgProtocol.GetRankResult newResult = resultBuilder.build();
            ctx.writeAndFlush(newResult);
            return null;
        });
    }
}
