
#include <iostream>
#include <istream>
#include <iomanip>
#include <ostream>
#include <string>

#include <gflags/gflags.h>
#include <glog/logging.h>

#include <ql/quantlib.hpp>

#include <boost/timer.hpp>

using namespace QuantLib;


DEFINE_string(test, "http", "Which test to run.");
DEFINE_string(host, "www.boost.org", "The host to connect to.");
DEFINE_string(path, "/LICENSE_1_0.txt", "The path.");
DEFINE_int32(port, 7777, "Port number (for EchoServer).");
DEFINE_int32(delay, 1, "Delay for events");

int main(int argc, char* argv[]) {

  google::SetUsageMessage("Prototype for the mongoose httpd.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);


  try {

    boost::timer timer;
    std::cout << std::endl;

    // set up dates
    Calendar calendar = TARGET();
    Date todaysDate(15, May, 1998);
    Date settlementDate(17, May, 1998);
    Settings::instance().evaluationDate() = todaysDate;

    // our options
    Option::Type type(Option::Put);
    Real underlying = 36;
    Real strike = 40;
    Spread dividendYield = 0.00;
    Rate riskFreeRate = 0.06;
    Volatility volatility = 0.20;
    Date maturity(17, May, 1999);
    DayCounter dayCounter = Actual365Fixed();

    std::cout << "Option type = "  << type << std::endl;
    std::cout << "Maturity = "        << maturity << std::endl;
    std::cout << "Underlying price = "        << underlying << std::endl;
    std::cout << "Strike = "                  << strike << std::endl;
    std::cout << "Risk-free interest rate = " << io::rate(riskFreeRate)
              << std::endl;
    std::cout << "Dividend yield = " << io::rate(dividendYield)
              << std::endl;
    std::cout << "Volatility = " << io::volatility(volatility)
              << std::endl;
    std::cout << std::endl;
    std::string method;
    std::cout << std::endl ;

    // write column headings
    Size widths[] = { 35, 14, 14, 14 };
    std::cout << std::setw(widths[0]) << std::left << "Method"
              << std::setw(widths[3]) << std::left << "American"
              << std::endl;

    std::vector<Date> exerciseDates;
    for (Integer i=1; i<=4; i++)
      exerciseDates.push_back(settlementDate + 3*i*Months);

    boost::shared_ptr<Exercise> americanExercise(
        new AmericanExercise(settlementDate,
                             maturity));

    Handle<Quote> underlyingH(
        boost::shared_ptr<Quote>(new SimpleQuote(underlying)));

    // bootstrap the yield/dividend/vol curves
    Handle<YieldTermStructure> flatTermStructure(
        boost::shared_ptr<YieldTermStructure>(
            new FlatForward(settlementDate, riskFreeRate, dayCounter)));
    Handle<YieldTermStructure> flatDividendTS(
        boost::shared_ptr<YieldTermStructure>(
            new FlatForward(settlementDate, dividendYield, dayCounter)));
    Handle<BlackVolTermStructure> flatVolTS(
        boost::shared_ptr<BlackVolTermStructure>(
            new BlackConstantVol(settlementDate, calendar, volatility,
                                 dayCounter)));
    boost::shared_ptr<StrikedTypePayoff> payoff(
        new PlainVanillaPayoff(type, strike));
    boost::shared_ptr<BlackScholesMertonProcess> bsmProcess(
        new BlackScholesMertonProcess(underlyingH, flatDividendTS,
                                      flatTermStructure, flatVolTS));

    // options
    VanillaOption americanOption(payoff, americanExercise);

    // Analytic formulas:

    // Barone-Adesi and Whaley approximation for American
    method = "Barone-Adesi/Whaley";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BaroneAdesiWhaleyApproximationEngine(bsmProcess)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Bjerksund and Stensland approximation for American
    method = "Bjerksund/Stensland";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BjerksundStenslandApproximationEngine(bsmProcess)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Finite differences
    Size timeSteps = 801;
    method = "Finite differences";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new FDAmericanEngine<CrankNicolson>(bsmProcess,
                                            timeSteps,timeSteps-1)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Jarrow-Rudd
    method = "Binomial Jarrow-Rudd";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<JarrowRudd>(bsmProcess,timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;
    method = "Binomial Cox-Ross-Rubinstein";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<CoxRossRubinstein>(bsmProcess,
                                                     timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Additive equiprobabilities
    method = "Additive equiprobabilities";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<AdditiveEQPBinomialTree>(bsmProcess,
                                                           timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Binomial Trigeorgis
    method = "Binomial Trigeorgis";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Trigeorgis>(bsmProcess,timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Binomial Tian
    method = "Binomial Tian";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Tian>(bsmProcess,timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Binomial Leisen-Reimer
    method = "Binomial Leisen-Reimer";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<LeisenReimer>(bsmProcess,timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // Binomial method: Binomial Joshi
    method = "Binomial Joshi";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Joshi4>(bsmProcess,timeSteps)));
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::endl;

    // End test
    Real seconds = timer.elapsed();
    Integer hours = int(seconds/3600);
    seconds -= hours * 3600;
    Integer minutes = int(seconds/60);
    seconds -= minutes * 60;
    std::cout << " \nRun completed in ";
    if (hours > 0)
      std::cout << hours << " h ";
    if (hours > 0 || minutes > 0)
      std::cout << minutes << " m ";
    std::cout << std::fixed << std::setprecision(0)
              << seconds << " s\n" << std::endl;
    return 0;

  } catch (std::exception& e) {
    std::cerr << e.what() << std::endl;
    return 1;
  } catch (...) {
    std::cerr << "unknown error" << std::endl;
    return 1;
  }
}
