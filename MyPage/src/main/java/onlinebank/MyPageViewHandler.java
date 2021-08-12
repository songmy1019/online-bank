package onlinebank;

import onlinebank.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {

    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenAccountRequestCompleted_then_UPDATE(@Payload AccountRequestCompleted accountRequestCompleted) {
        try {
            if (!accountRequestCompleted.validate()) return;
            System.out.println("\n\n##### listener accountRequestCompleted : " + accountRequestCompleted.toJson() + "\n\n");   

            MyPage myPage = myPageRepository.findByAccountNo( accountRequestCompleted.getAccountNo() );
            if( myPage == null ) myPage = new MyPage();            
            
            myPage.setAccountNo( accountRequestCompleted.getAccountNo() );
            myPage.setLastModifiedDate( new Date() );
            myPage.setLastRequestId( accountRequestCompleted.getRequestId() );
            myPage.setLastRequestName( accountRequestCompleted.getRequestName() );
            myPage.setRequestMoney( accountRequestCompleted.getRequestMoney() );
            myPage.setAmountOfMoney( accountRequestCompleted.getAmountOfMoney() );
            myPage.setRequestStatus("RequestCompleted");
            myPageRepository.save(myPage);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenAccountRequestCancelled_then_UPDATE(@Payload AccountRequestCancelled accountRequestCancelled) {
        try {
            if (!accountRequestCancelled.validate()) return;
            System.out.println("\n\n##### listener accountRequestCancelled : " + accountRequestCancelled.toJson() + "\n\n");   

            MyPage myPage = new MyPage();
            myPage.setAccountNo( accountRequestCancelled.getAccountNo() );
            myPage.setLastModifiedDate( new Date() );
            myPage.setLastRequestId( accountRequestCancelled.getRequestId() );
            myPage.setLastRequestName( accountRequestCancelled.getRequestName() );
            myPage.setRequestMoney( accountRequestCancelled.getRequestMoney() );
            myPage.setAmountOfMoney( accountRequestCancelled.getAmountOfMoney() );
            myPage.setRequestStatus("RequestCancelled");
            myPageRepository.save(myPage);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
