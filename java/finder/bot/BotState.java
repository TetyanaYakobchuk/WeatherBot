package finder.bot;

import finder.service.CacheService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import finder.service.UserService;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public enum BotState {

    //Начало
    Start {
        @Override
        public BotState nextState(BotContext context) {
            sendMessage(context, "Хочешь узнать погоду на 5 дней? Напиши - ок");
            return GetWeather;

        }
    },


    //возвращает погоду
    GetWeather {
        @Override
        public void handleInput(BotContext context) {
            sendMessage(context, "Минуточку! Идет обработка данных...");
            Document page = null;
            try {
                page = getPage();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // css query language
            Element tableWth = page.select("table[class=wt]").first();
            //System.out.println(tableWth);
            Elements names = tableWth.select("tr[class=wth]");
            Elements values = tableWth.select("tr[valign=top]");
            int index = 0;


            if (!CacheService.cacheIsEmpty()) {//проверяем, пуст ли кеш
                sendMessage(context, "(Data from cashe)");
                String result = CacheService.getResultStr();
                String[] res = result.split("~");

                for (int i = 0; i < res.length; i++) {
                    sendMessage(context, res[i]);//кидаем результат из кеша
                }

            } else {//если не пустой- возвращаем данные с сайта

                for (Element name : names) {
                    String dateString = name.select("th[id=dt]").text();
                    String date = null;
                    try {
                        date = getDateFromString(dateString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // sendMessage(context,date);
                    int iterationCount = printPartValues(values, index, context, date);
                    index = index + iterationCount;
                }
            }

        }

        @Override
        public BotState nextState(BotContext botContext) {
            return Start;//возвращаемся на начало
        }
    };


    private static Document getPage() throws IOException {
        String url = "http://www.pogoda.spb.ru/";
        Document page = Jsoup.parse(new URL(url), 3000);
        return page;
    }

    //\d символьный знак  --> \d{2}\.\d{2} регулярное выражение
    private static Pattern pattern = Pattern.compile("\\d{2}\\.\\d{2}");

    private static String getDateFromString(String stringDate) throws Exception {
        Matcher matcher = pattern.matcher(stringDate);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new Exception("Can't extract date from string!");


    }


    private static int printPartValues(Elements values, int index, BotContext botContext, String date) {
        int iterationCount = 4;
        int count = 0;
        if (index == 0) {
            Element valueLn = values.get(3);
            boolean isMorning = valueLn.text().contains("Утро");
            if (isMorning) {
                iterationCount = 3;
            }
        }


        StringBuffer resultStr = new StringBuffer();

        resultStr.append(date).append("\n");
        for (int i = 0; i < iterationCount; i++) {
            count = 0;
            Element valueLine = values.get(index + i);
            for (Element td : valueLine.select("td")) {

                if (count == 0) {
                    resultStr.append(td.text()).append("\n");
                }
                if (count == 1) {
                    resultStr.append("Явления : ").append(td.text()).append("\n");
                }
                if (count == 2) {
                    resultStr.append("Температура : ").append(td.text()).append("\n");
                }
                if (count == 3) {
                    resultStr.append("Давления : ").append(td.text()).append("\n");
                }
                if (count == 4) {
                    resultStr.append("Влажность : ").append(td.text()).append("\n");
                }
                if (count == 5) {
                    resultStr.append("Ветер : ").append(td.text()).append("\n");
                }
                count++;
            }
        }
        resultStr.append("~");
        CacheService.setResultStr(resultStr);//записываем в кеш

        sendMessage(botContext, resultStr.toString());

        System.out.println();

        return iterationCount;
    }


    private static BotState[] states;
    private final boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }


    public static BotState getInitialState() {
        return byId(0);
    }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values();
        }

        return states[id];
    }

    protected static void sendMessage(BotContext context, String text) {
        SendMessage message = new SendMessage()
                .setChatId(context.getUser().getChatId())
                .setText(text);
        try {
            context.getBot().execute(message);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    //метод созданный для переопределения в  state
    public void handleInput(BotContext context) {
        // do nothing by default
    }

    //метод созданный для переопределения в  state
    public void enter(BotContext context) {
        // do nothing by default
    }

    //метод созданный для переопределения в  state
    public BotState nextState(BotContext context) {
        return null;
    }


}
