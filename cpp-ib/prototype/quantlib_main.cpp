
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

DEFINE_bool(call, false, "Call");
DEFINE_bool(put, false, "Put");
DEFINE_double(underlying, 0., "Underlying");
DEFINE_double(strike, 0., "Strike");
DEFINE_double(volatility, 0.45, "Implied volatility as real number");
DEFINE_double(interestRate, 0.0025, "Interest rate as real number");
DEFINE_double(dividendYield, 0., "Dividend yield as real number");
DEFINE_int32(daysToExpiration, 0, "Days to expiration.");

static Size widths[] = { 35, 14, 14, 14 };

enum Greek { DELTA, GAMMA, THETA, VEGA, RHO, ITM_CASH_PROB };
inline std::string getParam(Greek greek, VanillaOption& option) {
  std::ostringstream oss;
  try {
    switch (greek) {
      case DELTA: oss << option.delta(); break;
      case GAMMA: oss << option.gamma(); break;
      case THETA: oss << option.theta(); break;
      case VEGA: oss << option.vega(); break;
      case RHO: oss << option.rho(); break;
      case ITM_CASH_PROB: oss << option.itmCashProbability(); break;
    }
  } catch (std::exception& e) {
    oss << "N/A";
  }
  return oss.str();
}

inline void printResult(std::string method, VanillaOption& americanOption) {
    std::cout << std::setw(widths[0]) << std::left << method
              << std::fixed
              << std::setw(widths[3]) << std::left << americanOption.NPV()
              << std::setw(widths[3]) << std::left << getParam(DELTA, americanOption)
              << std::setw(widths[3]) << std::left << getParam(GAMMA, americanOption)
              << std::setw(widths[3]) << std::left << getParam(THETA, americanOption)
              << std::setw(widths[3]) << std::left << getParam(VEGA, americanOption)
              << std::setw(widths[3]) << std::left << getParam(RHO, americanOption)
              << std::setw(widths[3]) << std::left << getParam(ITM_CASH_PROB, americanOption)
              << std::endl;
}

int main(int argc, char* argv[]) {

  google::SetUsageMessage("Prototype for the mongoose httpd.");
  google::ParseCommandLineFlags(&argc, &argv, true);
  google::InitGoogleLogging(argv[0]);


  try {

    boost::timer timer;
    std::cout << std::endl;

    // set up dates
    Calendar calendar = TARGET();
    Date todaysDate = Date::todaysDate();
    Date settlementDate = todaysDate;
    Date maturity = todaysDate + FLAGS_daysToExpiration;
    

    Settings::instance().evaluationDate() = todaysDate;

    // our options
    Option::Type type((FLAGS_call) ?
                      Option::Call :
                      (FLAGS_put ? Option::Put : Option::Call));
    Real underlying = FLAGS_underlying;
    Real strike = FLAGS_strike;
    Spread dividendYield = FLAGS_dividendYield;
    Rate riskFreeRate = FLAGS_interestRate;
    Volatility volatility = FLAGS_volatility;

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
    std::cout << std::setw(widths[0]) << std::left << "Method"
              << std::setw(widths[3]) << std::left << "American"
              << std::setw(widths[3]) << std::left << "Delta"
              << std::setw(widths[3]) << std::left << "Gamma"
              << std::setw(widths[3]) << std::left << "Theta"
              << std::setw(widths[3]) << std::left << "Vega"
              << std::setw(widths[3]) << std::left << "Rho"
              << std::setw(widths[3]) << std::left << "itmCashProbability"
              << std::endl;

    /*
    std::vector<Date> exerciseDates;
    for (Integer i=1; i<=4; i++)
      exerciseDates.push_back(settlementDate + 3*i*Months);
    */
    
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
    printResult(method, americanOption);

    // Bjerksund and Stensland approximation for American
    method = "Bjerksund/Stensland";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BjerksundStenslandApproximationEngine(bsmProcess)));
    printResult(method, americanOption);

    // Finite differences
    Size timeSteps = 801;
    method = "Finite differences";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new FDAmericanEngine<CrankNicolson>(bsmProcess,
                                            timeSteps,timeSteps-1)));
    printResult(method, americanOption);

    // Binomial method: Jarrow-Rudd
    method = "Binomial Jarrow-Rudd";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<JarrowRudd>(bsmProcess,timeSteps)));
    printResult(method, americanOption);

    // Binomial Cox-Ross-Rubinstein
    method = "Binomial Cox-Ross-Rubinstein";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<CoxRossRubinstein>(bsmProcess,
                                                     timeSteps)));
    printResult(method, americanOption);

    // Binomial method: Additive equiprobabilities
    method = "Additive equiprobabilities";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<AdditiveEQPBinomialTree>(bsmProcess,
                                                           timeSteps)));
    printResult(method, americanOption);

    // Binomial method: Binomial Trigeorgis
    method = "Binomial Trigeorgis";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Trigeorgis>(bsmProcess,timeSteps)));
    printResult(method, americanOption);

    // Binomial method: Binomial Tian
    method = "Binomial Tian";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Tian>(bsmProcess,timeSteps)));
    printResult(method, americanOption);

    // Binomial method: Binomial Leisen-Reimer
    method = "Binomial Leisen-Reimer";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<LeisenReimer>(bsmProcess,timeSteps)));
    printResult(method, americanOption);

    // Binomial method: Binomial Joshi
    method = "Binomial Joshi";
    americanOption.setPricingEngine(boost::shared_ptr<PricingEngine>(
        new BinomialVanillaEngine<Joshi4>(bsmProcess,timeSteps)));
    printResult(method, americanOption);

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
