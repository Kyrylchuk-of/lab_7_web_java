package com.mycompany.mywebapp;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Frame;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class App implements EntryPoint {
    /**
     * The message displayed to the user when the server cannot be reached or
     * returns an error.
     */
    private static final String SERVER_ERROR = "An error occurred while "
            + "attempting to contact the server. Please check your network "
            + "connection and try again.";
    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final GreetingServiceAsync greetingService = GWT
            .create(GreetingService.class);

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

        Button weatherButton = new Button("Show Weather");
        weatherButton.addClickHandler(event -> {
            Frame frame = new Frame("https://www.meteoblue.com/en/weather/week/rivne_ukraine_695594?utm_source=weather_widget&utm_medium=linkus&utm_content=three&utm_campaign=Weather%2BWidget");
            frame.setWidth("800px");
            frame.setHeight("600px");
            RootPanel.get("weatherContainer").clear();
            RootPanel.get("weatherContainer").add(frame);
            weatherButton.setVisible(false);
            RootPanel.get("weatherButton").setVisible(false);
        });
        RootPanel.get("weatherButton").add(weatherButton);
        weatherButton.setStyleName("custom-weather-button");

        final FormPanel form = new FormPanel();
        form.setAction("/myFormHandler");
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
        VerticalPanel panel = new VerticalPanel();
        panel.setStyleName("formPanel");
        form.setWidget(panel);
        final TextBox nameTextBox = new TextBox();
        nameTextBox.setName("name");
        nameTextBox.setStyleName("inputField");
        panel.add(new Label("Ім'я:"));
        panel.add(nameTextBox);
        final TextBox emailTextBox = new TextBox();
        emailTextBox.setName("email");
        emailTextBox.setStyleName("inputField");
        panel.add(new Label("Пошта:"));
        panel.add(emailTextBox);
        final TextBox phoneTextBox = new TextBox();
        phoneTextBox.setName("phone");
        phoneTextBox.setStyleName("inputField");
        panel.add(new Label("Телефон:"));
        panel.add(phoneTextBox);
        final TextBox messageTextBox = new TextBox();
        messageTextBox.setName("message");
        messageTextBox.setStyleName("inputField");
        panel.add(new Label("Повідомлення:"));
        panel.add(messageTextBox);
        Button submitButton = new Button("Відправити данні", new ClickHandler() {
            public void onClick(ClickEvent event) {
                form.submit();
            }
        });
        submitButton.setStyleName("submitButton");
        panel.add(submitButton);
        form.addSubmitHandler(new FormPanel.SubmitHandler() {
            public void onSubmit(SubmitEvent event) {
                if (nameTextBox.getText().isEmpty() || emailTextBox.getText().isEmpty() || phoneTextBox.getText().isEmpty() || messageTextBox.getText().isEmpty()) {
                    Window.alert("Будь ласка, заповніть усі поля");
                    event.cancel();
                }
            }
        });
        form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String name = nameTextBox.getText();
                String email = emailTextBox.getText();
                String phone = phoneTextBox.getText();
                String message = messageTextBox.getText();

                Window.alert("Ім'я: " + name + "\nПошта: " + email + "\nТелефон: " + phone + "\nПовідомлення: " + message);

            }
        });
        RootPanel formContainer = RootPanel.get("formContainer");
        formContainer.add(form);
        RootPanel headerPanel = RootPanel.get("header");
        headerPanel.getElement().setId("header");
        headerPanel.addStyleName("header");
        headerPanel.getElement().setInnerHTML("<h1 style='color: white' >Сайтик</h1>");
        String url = "https://api.privatbank.ua/p24api/pubinfo?json&exchange&coursid=5";
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            builder.sendRequest(null, new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    if (200 == response.getStatusCode()) {
                        displayExchangeRates(response.getText());
                    } else {
                        showErrorPanel(SERVER_ERROR);
                    }
                }

                public void onError(Request request, Throwable exception) {
                    showErrorPanel(SERVER_ERROR);
                }
            });
        } catch (RequestException e) {
            showErrorPanel(SERVER_ERROR);
        }
    }

    private void displayExchangeRates(String json) {
        JSONValue parsed = JSONParser.parseStrict(json);
        JSONArray ratesArray = parsed.isArray();
        if (ratesArray != null) {
            HTML ratesTable = createExchangeRatesTable(ratesArray);
            RootPanel.get("appleet").addStyleName("exchange-rates");
            RootPanel.get("appleet").add(ratesTable);
        } else {
            showErrorPanel("Не вдалося розпарсити JSON.");
        }
    }

    private HTML createExchangeRatesTable(JSONArray ratesArray) {
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        htmlBuilder.appendHtmlConstant("<table class='exchange-rates-table'>");
        htmlBuilder.appendHtmlConstant("<tr>")
                .appendHtmlConstant("<th>Валюта</th>")
                .appendHtmlConstant("<th>Базова валюта</th>")
                .appendHtmlConstant("<th>Купівля</th>")
                .appendHtmlConstant("<th>Продаж</th>")
                .appendHtmlConstant("</tr>");
        for (int i = 0; i < ratesArray.size(); ++i) {
            JSONObject rate = ratesArray.get(i).isObject();
            String ccy = rate.get("ccy").isString().stringValue();
            String base_ccy = rate.get("base_ccy").isString().stringValue();
            String buy = rate.get("buy").isString().stringValue();
            String sale = rate.get("sale").isString().stringValue();
            htmlBuilder.appendHtmlConstant("<tr>")
                    .appendHtmlConstant("<td>").appendEscaped(ccy).appendHtmlConstant("</td>")
                    .appendHtmlConstant("<td>").appendEscaped(base_ccy).appendHtmlConstant("</td>")
                    .appendHtmlConstant("<td>").appendEscaped(buy).appendHtmlConstant("</td>")
                    .appendHtmlConstant("<td>").appendEscaped(sale).appendHtmlConstant("</td>")
                    .appendHtmlConstant("</tr>");
        }
        htmlBuilder.appendHtmlConstant("</table>");
        return new HTML(htmlBuilder.toSafeHtml());
    }

    private void showErrorPanel(String errorMessage) {
        HTML errorLabel = new HTML("<div class='error-message'>" + errorMessage + "</div>");
        RootPanel.get().add(errorLabel);
    }
}