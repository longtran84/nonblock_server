package com.fintechviet.loyalty.controller;

import com.fintechviet.common.ErrorResponse;
import com.fintechviet.common.SuccessResponse;
import com.fintechviet.loyalty.service.LoyaltyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import play.i18n.Lang;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@Api(value = "Loyalty")
public class LoyaltyController extends Controller {
	private HttpExecutionContext ec;
	private LoyaltyService loyaltyService;
	private final MessagesApi messagesApi;

	@Inject()
	public LoyaltyController(LoyaltyService loyaltyService, HttpExecutionContext ec , MessagesApi messagesApi) {
		this.loyaltyService = loyaltyService;
		this.ec = ec;
		this.messagesApi = messagesApi;
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get phone cards")
	public CompletionStage<Result> getPhonecards()
			throws InterruptedException, ExecutionException {
		return loyaltyService.getPhonecards().thenApplyAsync(phonecards -> {
			return ok(Json.toJson(phonecards));
		}, ec.current());
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get gift codes")
	public CompletionStage<Result> getGiftcodes()
			throws InterruptedException, ExecutionException {
		return loyaltyService.getGiftcodes().thenApplyAsync(giftcodes -> {
			return ok(Json.toJson(giftcodes));
		}, ec.current());
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get game cards")
	public CompletionStage<Result> getGamecards()
			throws InterruptedException, ExecutionException {
		return loyaltyService.getGamecards().thenApplyAsync(gamecards -> {
			return ok(Json.toJson(gamecards));
		}, ec.current());
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get vouchers. Type = PHYSICAL_VOUCHER, E-VOUCHER")
	public CompletionStage<Result> getVouchers()
			throws InterruptedException, ExecutionException {
		return loyaltyService.getVouchers().thenApplyAsync(vouchers -> {
			return ok(Json.toJson(vouchers));
		}, ec.current());
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get voucher images")
	public CompletionStage<Result> getVoucherImages(int voucherId)
			throws InterruptedException, ExecutionException {
		return loyaltyService.getVoucherImages(voucherId).thenApplyAsync(voucherImages -> {
			return ok(Json.toJson(voucherImages));
		}, ec.current());
	}

	/**
	 * @return
	 */
	@ApiOperation(value = "Get voucher info")
	public CompletionStage<Result> getVoucherInfo(int voucherId)
			throws InterruptedException, ExecutionException {
		return loyaltyService.getVoucherInfo(voucherId).thenApplyAsync(voucher -> {
			return ok(Json.toJson(voucher));
		}, ec.current());
	}

	/**
	 * @param deviceToken
	 * @param itemId
	 * @param quantity
	 * @param  price
	 * @param type
	 * @return
	 */
	@ApiOperation(value = "Add item to cart. Type = VOUCHER, GAME_CARD, PHONE_CARD, GIFT_CODE")
	public CompletionStage<Result> addToCart(String deviceToken, int itemId, int quantity, double price, String type)
			throws InterruptedException, ExecutionException {
		return loyaltyService.addToCart(deviceToken, itemId, quantity, price, type).thenApplyAsync(response -> {
			return response.equals("ok") ? ok(Json.toJson(new SuccessResponse("ok"))) : badRequest(Json.toJson(new ErrorResponse(messagesApi.get(Lang.forCode("vi"), response))));
		}, ec.current());
	}

	/**
	 * @param deviceToken
	 * @return
	 */
	@ApiOperation(value = "Delete cart")
	public CompletionStage<Result> deleteCart(String deviceToken)
			throws InterruptedException, ExecutionException {
		return loyaltyService.deleteCart(deviceToken).thenApplyAsync(response -> {
			return response.equals("ok") ? ok(Json.toJson(new SuccessResponse("ok"))) : badRequest(Json.toJson(new ErrorResponse(messagesApi.get(Lang.forCode("vi"), "cart.delete.error"))));
		}, ec.current());
	}

	/**
	 * @param deviceToken
	 * @return
	 */
	@ApiOperation(value = "Get cart info")
	public CompletionStage<Result> getCartInfo(String deviceToken)
			throws InterruptedException, ExecutionException {
		return loyaltyService.getCartInfo(deviceToken).thenApplyAsync(cart -> {
			return ok(Json.toJson(cart));
		}, ec.current());
	}

	/**
	 * @param deviceToken
	 * @param customerName
	 * @param address
	 * @param phone
	 * @param email
	 * @return
	 */
	@ApiOperation(value = "Place order")
	public CompletionStage<Result> placeOrder(String deviceToken, String customerName, String address, String phone, String email)
			throws InterruptedException, ExecutionException {
		return loyaltyService.placeOrder(deviceToken, customerName, address, phone, email).thenApplyAsync(response -> {
			return response.equals("ok") ? ok(Json.toJson(new SuccessResponse(messagesApi.get(Lang.forCode("vi"),"order.place.success")))) : badRequest(Json.toJson(new ErrorResponse(messagesApi.get(Lang.forCode("vi"), response))));
		}, ec.current());
	}

	/**
	 * @param orderId
	 * @return
	 */
	@ApiOperation(value = "Get order info")
	public CompletionStage<Result> getOrderInfo(long orderId)
			throws InterruptedException, ExecutionException {
		return loyaltyService.getOrderInfo(orderId).thenApplyAsync(order -> {
			return ok(Json.toJson(order));
		}, ec.current());
	}

	/**
	 * @param deviceToken
	 * @return
	 */
	@ApiOperation(value = "Get order list. Status = NEW, PROCESSING, CANCELLED, SUCCESSFUL, CLOSE")
	public CompletionStage<Result> getOrders(String deviceToken)
			throws InterruptedException, ExecutionException {
		return loyaltyService.getOrders(deviceToken).thenApplyAsync(order -> {
			return ok(Json.toJson(order));
		}, ec.current());
	}

	/**
	 * @param orderId
	 * @return
	 */
	@ApiOperation(value = "Cancel order")
	public CompletionStage<Result> cancelOrder(long orderId)
			throws InterruptedException, ExecutionException {
		return loyaltyService.cancelOrder(orderId).thenApplyAsync(response -> {
			return response.equals("ok") ? ok(Json.toJson(new SuccessResponse(messagesApi.get(Lang.forCode("vi"), "order.cancel.ok")))) : badRequest(Json.toJson(new ErrorResponse(messagesApi.get(Lang.forCode("vi"), "order.cancel.error"))));
		}, ec.current());
	}
}
